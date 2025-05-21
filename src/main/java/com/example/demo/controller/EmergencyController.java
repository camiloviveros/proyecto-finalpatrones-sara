package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmergencyController {
    
    @GetMapping("/emergency-test")
    public String emergencyTest() {
        return "OK";
    }
}