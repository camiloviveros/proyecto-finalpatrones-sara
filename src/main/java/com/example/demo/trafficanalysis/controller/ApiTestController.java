package com.trafficanalysis.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ApiTestController {
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        log.info("Test API ping received");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "API funcionando correctamente");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check received");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        
        // Add memory info
        Runtime runtime = Runtime.getRuntime();
        status.put("memory_free_mb", runtime.freeMemory() / (1024 * 1024));
        status.put("memory_total_mb", runtime.totalMemory() / (1024 * 1024));
        status.put("memory_max_mb", runtime.maxMemory() / (1024 * 1024));
        
        return ResponseEntity.ok(status);
    }
}