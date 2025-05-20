package com.example.demo.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileWatcherService {

    private final JsonLoader jsonLoader;

    private static final String DIRECTORY_PATH = "../detections";  
    private static final String FILE_NAME = "detections.json";  

    @PostConstruct
    @Async
    public void watchFileChanges() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(DIRECTORY_PATH);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            log.info("Observando cambios en {}", path.toAbsolutePath());

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changedFile = (Path) event.context();

                    if (changedFile.toString().equals(FILE_NAME)) {
                        log.info("Archivo {} {}", FILE_NAME, (kind == StandardWatchEventKinds.ENTRY_MODIFY ? "modificado" : "creado"));

                        try {
                            jsonLoader.loadJsonAndSaveToDb(DIRECTORY_PATH + "/" + FILE_NAME);
                            log.info("Datos cargados y guardados en la base de datos");
                        } catch (IOException e) {
                            log.error("Error al leer o procesar el archivo JSON: {}", e.getMessage(), e);
                        } catch (IllegalArgumentException e) {
                            log.error("Argumento inválido al procesar los datos: {}", e.getMessage(), e);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    log.warn("WatchKey inválido. Terminando observación.");
                    break;
                }
            }

        } catch (IOException e) {
            log.error("Error al crear o registrar el servicio de observación de archivos: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Proceso de observación interrumpido: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
        }
    }
}