package com.example.ageestimationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgeEstimationResponse {

    private Long id;
    private String imagePath;
    private Integer predictedAge;
    private Double confidence;
    private String fileName;
    private Long imageSize;
    private Long processingTimeMs;
    private LocalDateTime timestamp;
    private String message;
    private boolean success;
}
