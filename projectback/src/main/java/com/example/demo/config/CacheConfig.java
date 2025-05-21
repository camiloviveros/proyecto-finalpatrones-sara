package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * Configuración del sistema de caché utilizando Caffeine.
 * Esta configuración mejora significativamente los tiempos de respuesta
 * al almacenar temporalmente los resultados de consultas costosas.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Define el administrador de caché con Caffeine.
     * 
     * @return Un gestor de caché configurado.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineConfig());
        return cacheManager;
    }

    /**
     * Configura las propiedades de la caché Caffeine.
     * - Tamaño máximo: 500 entradas
     * - Tiempo de expiración: 5 minutos después de la última escritura
     * - Tiempo de inactividad: 10 minutos
     * 
     * @return Una instancia de Caffeine configurada
     */
    private Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats(); // Para métricas de rendimiento
    }
}