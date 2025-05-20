// projectback/src/main/java/com/example/demo/service/DetectionService.java
package com.example.demo.service;

import com.example.demo.entity.Detection;
import com.example.demo.repository.DetectionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j  // Añadimos anotación para logging
public class DetectionService {

    private final DetectionRepository detectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveDetections(List<Detection> detections) {
        detectionRepository.saveAll(detections);
    }

    public List<Detection> getAllDetections() {
        return detectionRepository.findAll();
    }

    public Map<String, Integer> parseObjectsTotal(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            // Reemplazamos printStackTrace() con log.error()
            log.error("Error al analizar objectsTotal JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    public Map<String, Map<String, Integer>> parseObjectsByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, Integer>>>() {});
        } catch (JsonProcessingException e) {
            // Reemplazamos printStackTrace() con log.error()
            log.error("Error al analizar objectsByLane JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    public Map<String, Double> parseAvgSpeedByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            // Reemplazamos printStackTrace() con log.error()
            log.error("Error al analizar avgSpeedByLane JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}