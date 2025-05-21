package com.trafficanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrafficAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficAnalysisApplication.class, args);
    }
}