package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.demo.service.JsonLoader;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync

public class ProjectbackApplication {

    private final JsonLoader jsonLoader;

    public static void main(String[] args) {
        SpringApplication.run(ProjectbackApplication.class, args);
    }

    @PostConstruct
    public void runOnStartup() throws Exception {
        String filePath = "../detections/detections.json";  // Ruta relativa al archivo JSON
        jsonLoader.loadJsonAndSaveToDb(filePath);
        System.out.println("Detections importadas");
    }
}