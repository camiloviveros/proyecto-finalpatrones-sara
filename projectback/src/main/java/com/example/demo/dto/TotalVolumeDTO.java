package com.example.demo.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalVolumeDTO {
    private Map<String, Integer> hourly;
    private Map<String, Integer> daily;
    private Map<String, Integer> total;
}