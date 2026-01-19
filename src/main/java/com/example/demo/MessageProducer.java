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
        // Senden an das FINAL Topic
        kafkaTemplate.send("warehouse-final-topic", data).whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("LAGER LINZ -> Gesendet an: " + result.getRecordMetadata().topic() + " (Offset: " + result.getRecordMetadata().offset() + ")");
            }
        });
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