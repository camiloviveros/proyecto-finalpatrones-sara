package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicController {
    
    @GetMapping("/basic-test")
    public String basicTest() {
        return "OK-BASIC";
    }
    
    @GetMapping("/api/basic-test")
    public Map<String, Object> apiBasicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API is working");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}