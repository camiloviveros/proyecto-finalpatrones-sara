package com.example.demo.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de caché para almacenar datos temporalmente y mejorar los tiempos de respuesta.
 * Implementa una estrategia de expiración basada en tiempo.
 */
@Service
@Slf4j
public class CacheService {

    // Estructura para almacenar los datos en caché
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // Tiempo de expiración predeterminado en milisegundos (5 minutos)
    private static final long DEFAULT_EXPIRATION = 300000; 

    /**
     * Almacena un valor en la caché con el tiempo de expiración predeterminado.
     * 
     * @param key Clave para identificar el valor
     * @param value Valor a almacenar
     */
    public void put(String key, Object value) {
        put(key, value, DEFAULT_EXPIRATION);
    }

    /**
     * Almacena un valor en la caché con un tiempo de expiración personalizado.
     * 
     * @param key Clave para identificar el valor
     * @param value Valor a almacenar
     * @param expirationMs Tiempo de expiración en milisegundos
     */
    public void put(String key, Object value, long expirationMs) {
        long expirationTime = Instant.now().toEpochMilli() + expirationMs;
        cache.put(key, new CacheEntry(value, expirationTime));
        log.debug("Valor almacenado en caché: {} (expira en {} ms)", key, expirationMs);
    }

    /**
     * Obtiene un valor de la caché si existe y no ha expirado.
     * 
     * @param key Clave del valor a obtener
     * @return El valor almacenado o null si no existe o ha expirado
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            log.debug("Cache miss: {}", key);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("Entrada expirada en caché: {}", key);
            cache.remove(key);
            return null;
        }
        
        log.debug("Cache hit: {}", key);
        return entry.getValue();
    }

    /**
     * Verifica si existe una entrada en la caché con la clave proporcionada y no ha expirado.
     * 
     * @param key Clave a verificar
     * @return true si existe y no ha expirado, false en caso contrario
     */
    public boolean contains(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }

    /**
     * Elimina una entrada de la caché.
     * 
     * @param key Clave de la entrada a eliminar
     */
    public void remove(String key) {
        cache.remove(key);
        log.debug("Entrada eliminada de caché: {}", key);
    }

    /**
     * Limpia toda la caché.
     */
    public void clear() {
        cache.clear();
        log.info("Caché completamente limpiada");
    }

    /**
     * Clase interna para representar una entrada en la caché con su tiempo de expiración.
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;
        
        CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        Object getValue() {
            return value;
        }
        
        boolean isExpired() {
            return Instant.now().toEpochMilli() > expirationTime;
        }
    }
}