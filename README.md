# DEZSYS_GK73_WAREHOUSE_MOM
Join GIT repository:  https://github.com/ThomasMicheler/DEZSYS_GK73_WAREHOUSE_MOM.git

## Einführung

Diese Übung soll die Funktionsweise und Implementierung von eine Message Oriented Middleware (MOM) mit Hilfe des **Frameworks Apache Kafka** demonstrieren. **Message Oriented Middleware (MOM)** ist neben InterProcessCommunication (IPC), Remote Objects (RMI) und Remote Procedure Call (RPC) eine weitere Möglichkeit um eine Kommunikation zwischen mehreren Rechnern umzusetzen.

Die Umsetzung bas
Die Umsetzung basiert auf einem praxisnahen Beispiel eines Warenlagers. Die Zentrale des Warenlagers moechte jede Stunde den aktuellen Lagerstand aller Lagerstandorte abfragen.

Mit diesem Ziel soll die REST-Applikation aus MidEng 7.1 Warehouse REST and Dataformats bei einem entsprechenden Request http://<IP Wahllokal>/warehouse/send die Daten (JSON oder XML) in eine Message Queue der Zentral uebertragen. 
In regelmaessigen Abstaenden werden alle Message Queues der Zentrale abgefragt und die Daten aller Standorte gesammelt.

Die gesammelten Lagerstände werden ueber eine REST-Schnittstelle (in XML oder JSON) dem Berichtswesen des Managements zur Verfuegung gestellt.

## 1.1 Ziele  

Das Ziel dieser Übung ist die **Implementierung einer Kommunikationsplattform für Warenlager. Dabei erfolgt ein Datenaustausch von mehreren Lagerstandorten mit der Zentrale unter Verwendung einer Message Oriented Middleware (MOM)**. Die einzelnen Daten des Warenlagers sollen an die Zentrale übertragen werden. Es sollen **nachrichtenbasierten Protokolle mit Message Queues** verwendet werden. Durch diese lose Kopplung kann gewährleistet werden, dass in Zukunft weitere Standorte hinzugefügt bzw. Kooperationspartner eingebunden werden können.

Fuer die REST-Schnittstelle in der Zentralle muessen die Datenstrukturen der einzelnene Lagerstandorte zusammengefasst werden. Um die Datenintegrität zu garantieren, sollen jene Daten, die mit der Middleware übertragen werden in einer LOG-Datei abgespeichert werden.  

## 1.2 Voraussetzungen

*   Grundlagen Architektur von verteilten Systemen
*   Grundlagen zur nachrichtenbasierten Systemen / Message Oriented Middleware  
*   Verwendung des Message Brokers Apache Kafka
*   Verwendung der XML- oder JSON Datenstruktur des Wahllokals
*   Verwendung der Demo-Applikation MOMApplication (inklusive MOMReceiver und MOMSender) (siehe Repo)

## 1.3 Aufgabenstellung

Implementieren Sie die Lager-Kommunikationsplattform mit Hilfe des Java Message Service. Verwenden Sie Apache Kafka ([https://kafka.apache.org](https://kafka.apache.org)) als Message Broker Ihrer Applikation. Das Programm soll folgende Funktionen beinhalten:

 *   Installation von Apache Kafka in der Zentrale.
 *   Jeder Lagerstandort hat eine Message Queue mit einer ID am zentralen Rechner.
 *   Jeder Lagerstandort legt in regelmässigen Abständen die Daten des Lagers in der Message Queue ab.
 *   Bei einer erfolgreichen Übertragung sendet die Zentrale die Nachricht "SUCCESS" an den Lagerstandort retour.
 *   Der zentrale Rechner fragt in regelmässigen Abständen alle Message Queues ab.
 *   Der Zentralrechner fuegt alle Daten aller Lagerstandorte zusammen und stellt diese an einer REST Schnittstelle im JSON/XML Format zur Verfügung.

## 1.4 Demo Applikation

*   Installation und starten des Message Broker Apache Kafka (Container)  
[https://kafka.apache.org/quickstart](https://kafka.apache.org/quickstart)    

*   Erstellen einer Message Queue "quickstart-events" (Terminal/Container)   
     `cd /opt/kafka`   
     `bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092`    

*  Senden von Nachrichten (via Terminal)    
    `bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092`   
    `> Hallo Spencer, hier ist Nachricht 1.`   
    `> Hallo Spencer, hier ist Nachricht 2`   
    `> Hallo Spencer, hier ist Nachricht 3.`   

*  Lesen von Nachrichten (via Terminal)   
    `bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092`   

### 1.4.1 warehouse_demo

Demo 1 beinhaltet eine Implementierung, die alle Einzelschritte zur Implementierung von Java und JMS beinhaltet und uebersichtlich darstellt. 

 *   Starten der Demo Applikation 
     `gradle clean bootRun`

 *   Senden einer Nachricht 
      http://localhost:8080/send?message=Hallo Spencer

 *   Empfang der Nachricht auf der Konsole
      Hallo Spencer

## 1.5 Bewertung  

*   Gruppengrösse: 1 Person
*   Abgabemodus: per Protokoll, bei EK kann ein Abgabegespraech erforderlich sein
*   Anforderungen **"Grundlagen"**
    *   Implementierung der Kommunikation zwischen **EINEM** Lagerstandort und dem Zentralrechner (JMS Queue)  
    *   Ausgabe der empfangenen Daten am Zentralrechner (Konsole oder Log-Datei)
    *   Beantwortung der Fragestellungen   
*   Anforderungen **"Erweiterte Grundlagen"**
    *   Zusammensetzung der Daten aller Lagerstandorte in einer zentralen JSON/XML-Struktur
    *   Implementierung der REST Schnittstelle am Zentralrechner
*  Erweiterte Anforderungen **"Vertiefung"**
    *   Implementierung der Kommunikation mit **MEHREREN** Lagerstandorte und dem Zentralrechner
    *   Logging der Daten bei aller Lagerstandorte und dem Zentralrechner   
    *   Rückmeldung des Ergebnisses der Übertragung vom Zentralrechner an den einzelnen Lagerstandort (JMS Topic)  

## 1.6 Fragestellung für Protokoll

*   Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?  
*   Was versteht man unter einer transienten und synchronen Kommunikation?
*   Beschreiben Sie die Funktionsweise einer JMS Queue?
*   JMS Overview - Beschreiben Sie die wichtigsten JMS Klassen und deren Zusammenhang?
*   Beschreiben Sie die Funktionsweise eines JMS Topic?
*   Was versteht man unter einem lose gekoppelten verteilten System? Nennen Sie ein Beispiel dazu. Warum spricht man hier von lose?
`
## 1.6 Links & Dokumente

*   Grundlagen Message Oriented Middleware: [Presentation](https://elearning.tgm.ac.at/pluginfile.php/119077/mod_resource/content/1/dezsys_mom_einfuehrung.pdf)
*   Middleware:  [Apache Kafka](https://kafka.apache.org/quickstart)  
*   [Apache Kafka | Getting Started](https://kafka.apache.org/documentation/#gettingStarted)   

    
  https://medium.com/@abhishekranjandev/a-comprehensive-guide-to-integrating-kafka-in-a-spring-boot-application-a4b912aee62e
  https://spring.io/guides/gs/messaging-jms/  
  https://medium.com/@mailshine/activemq-getting-started-with-springboot-a0c3c960356e   
  http://www.academictutorials.com/jms/jms-introduction.asp   
  http://docs.oracle.com/javaee/1.4/tutorial/doc/JMS.html#wp84181    
  https://www.oracle.com/java/technologies/java-message-service.html   
  http://www.oracle.com/technetwork/articles/java/introjms-1577110.html  
  https://spring.io/guides/gs/messaging-jms  
  https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-messaging.html  
  https://dzone.com/articles/using-jms-in-spring-boot-1  
