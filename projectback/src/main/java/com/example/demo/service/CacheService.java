// Modificar src/main/java/com/example/demo/service/CacheService.java
package com.example.demo.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheService {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRATION = 300000;
    private final ReentrantLock lock = new ReentrantLock();
    
    public void put(String key, Object value) {
        put(key, value, DEFAULT_EXPIRATION);
    }

    public void put(String key, Object value, long expirationMs) {
        try {
            if (lock.tryLock(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                try {
                    long expirationTime = Instant.now().toEpochMilli() + expirationMs;
                    cache.put(key, new CacheEntry(value, expirationTime));
                    log.debug("Valor almacenado en caché: {} (expira en {} ms)", key, expirationMs);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("No se pudo obtener el bloqueo para la caché - operación ignorada");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupción al intentar obtener el bloqueo de caché", e);
        }
    }

    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            log.debug("Cache miss: {}", key);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("Entrada expirada en caché: {}", key);
            remove(key);
            return null;
        }
        
        log.debug("Cache hit: {}", key);
        return entry.getValue();
    }

    public boolean contains(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            remove(key);
            return false;
        }
        
        return true;
    }

    public void remove(String key) {
        try {
            if (lock.tryLock(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                try {
                    cache.remove(key);
                    log.debug("Entrada eliminada de caché: {}", key);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupción al intentar obtener el bloqueo para eliminar caché", e);
        }
    }

    public void clear() {
        try {
            if (lock.tryLock(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                try {
                    cache.clear();
                    log.info("Caché completamente limpiada");
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupción al intentar obtener el bloqueo para limpiar caché", e);
        }
    }

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