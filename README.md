## 1. Einführung und Zielsetzung

Ziel dieser Übung war die Implementierung eines verteilten Systems zur Übermittlung von Lagerbeständen zwischen mehreren Standorten und einer Zentrale. Als Middleware wurde Apache Kafka eingesetzt, um eine lose Kopplung und asynchrone Kommunikation zu gewährleisten. Die Zentrale führt die Daten zusammen und stellt sie über eine REST-Schnittstelle zur Verfügung.

---

## 2. Systemarchitektur & Konfiguration

### 2.1 Infrastruktur (Docker)

Um eine saubere Testumgebung zu schaffen, wurde Apache Kafka mittels Docker Compose im KRaft-Modus (ohne Zookeeper) betrieben.

**docker-compose.yml**

```yaml
services:
  kafka:
    image: apache/kafka:3.8.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      CLUSTER_ID: mkU3O9S6S3m5X7qP2n6Otw
```

### 2.2 Applikations-Konfiguration (Spring Boot)

In der application.yml wurden die Serialisierung sowie das Verhalten des Consumers definiert.

**application.yml**

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: 1
```

---

## 3. Implementierung

### 3.1 Lager-Simulator (MessageProducer)

Der Producer simuliert zwei Standorte (Linz und Wien), die in unterschiedlichen Intervallen Daten senden. Er empfängt zudem Bestätigungen von der Zentrale.

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 10000)
    public void sendFromLinz() {
        String data = "{\"id\": \"Warehouse_Linz\", \"item\": \"Reifen\", \"count\": 40}";
        kafkaTemplate.send("warehouse-final-topic", data);
    }

    @Scheduled(fixedRate = 15000)
    public void sendFromWien() {
        String data = "{\"id\": \"Warehouse_Wien\", \"item\": \"Felgen\", \"count\": 100}";
        kafkaTemplate.send("warehouse-final-topic", data);
    }

    @KafkaListener(topics = "warehouse-responses", groupId = "final-lager-group")
    public void listenForSuccess(String response) {
        System.out.println("LAGER-INFO: Rückmeldung erhalten -> " + response);
    }
}
```

### 3.2 Zentrale (MessageConsumer)

Der Consumer empfängt die Daten, führt sie in einer ConcurrentHashMap zusammen und sendet eine SUCCESS-Nachricht zurück.

```java
package com.example.demo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageConsumer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Map<String, String> warehouseStore = new ConcurrentHashMap<>();

    @KafkaListener(topics = "warehouse-final-topic", 
                   groupId = "zentrale-final-#{T(java.util.UUID).randomUUID().toString()}")
    public void listen(String message) {
        if (message.contains("Linz")) {
            warehouseStore.put("Linz", message);
        } else if (message.contains("Wien")) {
            warehouseStore.put("Wien", message);
        }
        kafkaTemplate.send("warehouse-responses", "SUCCESS: Verarbeitet.");
    }

    public Map<String, String> getAllData() {
        return warehouseStore;
    }
}
```

### 3.3 REST-Schnittstelle (WarehouseController)

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private MessageConsumer messageConsumer;

    @GetMapping("/data")
    public Map<String, String> getWarehouseData() {
        return messageConsumer.getAllData();
    }
}
```

---

## 4. Dokumentation der Fehlerbehebung (Reflektion)

Während der Entwicklung traten folgende Probleme auf, die systematisch gelöst wurden:

1. **Fehler:** Unnamed Classes Error (Java 21 Preview).

   - **Ursache:** Methoden wurden außerhalb einer definierten Klasse geschrieben.
   - **Lösung:** Korrekte Kapselung des Codes in public class Strukturen.
2. **Fehler:** REST-Schnittstelle liefert 404.

   - **Ursache:** Falsche Package-Struktur; Spring Boot konnte den Controller nicht finden.
   - **Lösung:** Sicherstellung, dass alle Klassen im Unterverzeichnis des @SpringBootApplication-Pakets liegen.
3. **Fehler:** Producer sendet (Offsets steigen), aber Consumer empfängt nichts (Received: 0 records).

   - **Ursache:** Problem mit Consumer-Offsets. Die Gruppe war bereits registriert und "dachte", sie hätte alle Nachrichten gelesen.
   - **Lösung:** Implementierung einer dynamischen groupId mittels UUID und Nutzung eines frischen Topics (warehouse-final-topic). Dadurch wurde ein Replay aller Nachrichten ab Offset 0 erzwungen.
4. **Fehler:** Unknown Topic or Partition.

   - **Ursache:** Der Consumer versuchte zu lesen, bevor der Producer das Topic durch die erste Nachricht erstellt hatte.
   - **Lösung:** Ignorieren der initialen Warnung, da Kafka das Topic bei der ersten Interaktion automatisch erstellt (Auto-Creation).

---

## 5. Beantwortung der Fragestellungen

**1. Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?**

- **Asynchronität:** Sender und Empfänger agieren zeitlich versetzt.
- **Lose Kopplung:** Komponenten kennen nur das Nachrichtenformat, nicht die Gegenstelle.
- **Persistenz:** Nachrichten werden zwischengespeichert, falls ein Teilnehmer offline ist.
- **Skalierbarkeit:** Einfaches Hinzufügen weiterer Producer/Consumer.

**2. Was versteht man unter einer transienten und synchronen Kommunikation?**

- **Synchron:** Der Sender blockiert und wartet auf eine direkte Antwort (z.B. HTTP).
- **Transient:** Die Nachricht wird nicht gespeichert; ist der Empfänger nicht online, geht sie verloren.

**3. Beschreiben Sie die Funktionsweise einer JMS Queue?**

- Es handelt sich um ein Point-to-Point Modell. Eine Nachricht wird an genau einen Empfänger zugestellt und danach gelöscht.

**4. JMS Overview - Wichtigste Klassen?**

- ConnectionFactory, Connection, Session, MessageProducer, MessageConsumer, Destination (Queue/Topic).

**5. Beschreiben Sie die Funktionsweise eines JMS Topic?**

- Publish/Subscribe Modell. Eine Nachricht wird an alle aktiven Abonnenten (Subscriber) gleichzeitig verteilt.

**6. Was versteht man unter einem lose gekoppelten verteilten System?**

- Systeme, die minimale Abhängigkeiten haben. Ein Ausfall oder eine Änderung in einem Teilsystem (z.B. Lager) beeinträchtigt nicht direkt die Funktion des anderen (Zentrale), solange die Middleware (Kafka) die Kommunikation puffert.

---
