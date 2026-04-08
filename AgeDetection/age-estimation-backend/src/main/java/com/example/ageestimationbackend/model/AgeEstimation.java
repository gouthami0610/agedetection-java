package com.example.ageestimationbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "age_estimations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgeEstimation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "predicted_age", nullable = false)
    private Integer predictedAge;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "image_size")
    private Long imageSize;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
