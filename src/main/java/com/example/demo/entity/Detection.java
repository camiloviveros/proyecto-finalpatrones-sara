package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Detection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timestampMs;
    private String date;

    @Column(columnDefinition = "TEXT")
    private String objectsTotal;

    @Column(columnDefinition = "TEXT")
    private String objectsByLane;

    @Column(columnDefinition = "TEXT")
    private String avgSpeedByLane;
}
