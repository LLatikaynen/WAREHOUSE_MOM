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
        System.out.println(">>> REST-API wurde aufgerufen! Aktueller Speicherstand: " + messageConsumer.getAllData().size());
        return messageConsumer.getAllData();
    }
}