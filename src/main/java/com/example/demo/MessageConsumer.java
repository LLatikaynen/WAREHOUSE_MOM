package com.example.demo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class MessageConsumer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Map<String, String> warehouseStore = new ConcurrentHashMap<>();

     @KafkaListener(topics = "warehouse-final-topic",
            groupId = "zentrale-final-#{T(java.util.UUID).randomUUID().toString()}")
    public void listen(String message) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("ZENTRALE: Paket endlich erhalten: " + message);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            if (message.contains("Linz")) {
                warehouseStore.put("Linz", message);
            } else if (message.contains("Wien")) {
                warehouseStore.put("Wien", message);
            } else {
                warehouseStore.put("Unbekannt", message);
            }

            // Rückmeldung senden
            kafkaTemplate.send("warehouse-responses", "SUCCESS: Verarbeitet.");
        } catch (Exception e) {
            System.err.println(">>> [FEHLER] Fehler beim Verarbeiten der Nachricht: " + e.getMessage());
        }
    }

    public Map<String, String> getAllData() {
        return warehouseStore;
    }
}