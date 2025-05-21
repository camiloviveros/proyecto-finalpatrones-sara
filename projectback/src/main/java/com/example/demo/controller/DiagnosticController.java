package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Verificar la base de datos
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", "UP");
            response.put("db_test", result);
        } catch (CannotGetJdbcConnectionException e) {
            response.put("database", "DOWN");
            response.put("error", "Error de conexión: " + e.getMessage());
        } catch (BadSqlGrammarException e) {
            response.put("database", "ERROR");
            response.put("error", "Error de SQL: " + e.getMessage());
        } catch (DataAccessException e) {
            response.put("database", "DOWN");
            response.put("error", "Error de acceso a datos: " + e.getMessage());
        }
        
        // Información sobre memoria
        Runtime runtime = Runtime.getRuntime();
        response.put("memory_free_mb", runtime.freeMemory() / (1024 * 1024));
        response.put("memory_total_mb", runtime.totalMemory() / (1024 * 1024));
        response.put("memory_max_mb", runtime.maxMemory() / (1024 * 1024));
        
        return response;
    }
    
    @GetMapping("/detection-count")
    public Map<String, Object> getDetectionCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM detection", Integer.class);
            response.put("count", count);
            response.put("status", "success");
        } catch (CannotGetJdbcConnectionException e) {
            response.put("status", "error");
            response.put("error", "Error de conexión a la base de datos: " + e.getMessage());
        } catch (BadSqlGrammarException e) {
            response.put("status", "error");
            response.put("error", "Error en la consulta SQL: " + e.getMessage());
        } catch (DataAccessException e) {
            response.put("status", "error");
            response.put("error", "Error de acceso a datos: " + e.getMessage());
        }
        
        return response;
    }
}