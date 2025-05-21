package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Detection;
import com.example.demo.service.DTO.DetectionsWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonLoader {

    private static final Logger log = LoggerFactory.getLogger(JsonLoader.class);
    
    private final DetectionService detectionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JsonLoader(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @Transactional
    public void loadJsonAndSaveToDb(String filePath) throws IOException {
        log.info("Leyendo el archivo JSON desde: {}", filePath);

        try {
            File jsonFile = new File(filePath);
            if (!jsonFile.exists()) {
                throw new IOException("El archivo no existe: " + filePath);
            }

            DetectionsWrapper wrapper = objectMapper.readValue(jsonFile, DetectionsWrapper.class);
            log.info("Archivo JSON cargado exitosamente.");
            
            List<Detection> detections = wrapper.getDetections().stream()
                .map(d -> {
                    log.debug("Procesando detección con timestamp_ms: {}", d.getTimestamp_ms());
                    
                    // Crear manualmente sin builder
                    Detection detection = new Detection();
                    detection.setTimestampMs(d.getTimestamp_ms());
                    detection.setDate(d.getDate());
                    detection.setObjectsTotal(safeWriteValueAsString(d.getObjects_total()));
                    detection.setObjectsByLane(safeWriteValueAsString(d.getObjects_by_lane()));
                    detection.setAvgSpeedByLane(safeWriteValueAsString(d.getAvg_speed_by_lane()));
                    return detection;
                })
                .collect(Collectors.toList());
            
            log.info("Datos procesados. Guardando en la base de datos...");
            detectionService.saveDetections(detections);
            
            log.info("Detecciones cargadas exitosamente en la base de datos.");
        } catch (IOException e) {
            log.error("Error al leer o procesar el archivo JSON: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String safeWriteValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            log.error("Error al convertir el objeto a JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
}