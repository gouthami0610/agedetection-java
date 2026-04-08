#!/bin/bash

# 1. Create Directory Structure
echo "Creating project structure..."
mkdir -p age-estimation-backend/src/main/java/com/example/ageestimation/config
mkdir -p age-estimation-backend/src/main/java/com/example/ageestimation/controller
mkdir -p age-estimation-backend/src/main/java/com/example/ageestimation/model
mkdir -p age-estimation-backend/src/main/java/com/example/ageestimation/repository
mkdir -p age-estimation-backend/src/main/java/com/example/ageestimation/service
mkdir -p age-estimation-backend/src/main/resources/models
mkdir -p age-estimation-backend/uploads

cd age-estimation-backend

# 2. Create pom.xml
echo "Creating pom.xml..."
cat << 'EOF' > pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>age-estimation-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>age-estimation-backend</name>
    <description>Age Estimation System using OpenCV and Spring Boot</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.openpnp</groupId>
            <artifactId>opencv</artifactId>
            <version>4.9.0-0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# 3. Create application.properties
echo "Creating application.properties..."
cat << 'EOF' > src/main/resources/application.properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/age_estimation_db?createDatabaseIfNotExist=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads
app.model.path=src/main/resources/models/
EOF

# 4. Create Java Files

# Main Application
echo "Creating AgeEstimationApplication.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/AgeEstimationApplication.java
package com.example.ageestimation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgeEstimationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgeEstimationApplication.class, args);
    }
}
EOF

# Config
echo "Creating OpenCVConfig.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/config/OpenCVConfig.java
package com.example.ageestimation.config;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCVConfig {
    @PostConstruct
    public void loadOpenCV() {
        OpenCV.loadLocally();
        System.out.println("OpenCV Native Libraries Loaded Successfully.");
    }
}
EOF

# Model
echo "Creating PredictionRecord.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/model/PredictionRecord.java
package com.example.ageestimation.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_records")
@Data
public class PredictionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalFileName;
    private String storedFilePath;
    private String predictedAgeRange;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
EOF

# Repository
echo "Creating PredictionRepository.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/repository/PredictionRepository.java
package com.example.ageestimation.repository;

import com.example.ageestimation.model.PredictionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionRepository extends JpaRepository<PredictionRecord, Long> {
}
EOF

# Service
echo "Creating AgeService.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/service/AgeService.java
package com.example.ageestimation.service;

import com.example.ageestimation.model.PredictionRecord;
import com.example.ageestimation.repository.PredictionRepository;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AgeService {

    private final PredictionRepository repository;
    private final String uploadDir;
    
    private static final String[] AGE_BUCKETS = {
            "0-2", "4-6", "8-12", "15-20", "25-32", "38-43", "48-53", "60-100"
    };

    private static final String PROTO_PATH = "src/main/resources/models/deploy.prototxt";
    private static final String MODEL_PATH = "src/main/resources/models/age_net.caffemodel";

    public AgeService(PredictionRepository repository, @Value("${app.upload.dir}") String uploadDir) {
        this.repository = repository;
        this.uploadDir = uploadDir;
        initUploadDir();
    }

    private void initUploadDir() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    public PredictionRecord processImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String predictedAge = estimateAge(targetLocation.toString());

        PredictionRecord record = new PredictionRecord();
        record.setOriginalFileName(file.getOriginalFilename());
        record.setStoredFilePath(targetLocation.toString());
        record.setPredictedAgeRange(predictedAge);

        return repository.save(record);
    }

    private String estimateAge(String imagePath) {
        File protoFile = new File(PROTO_PATH);
        File modelFile = new File(MODEL_PATH);

        if (!protoFile.exists() || !modelFile.exists()) {
            System.out.println("WARN: Model files not found. Returning simulated result.");
            return "25-32 (Simulation)";
        }

        try {
            Net net = Dnn.readNetFromCaffe(PROTO_PATH, MODEL_PATH);
            Mat image = Imgcodecs.imread(imagePath);
            if (image.empty()) {
                throw new RuntimeException("Failed to load image via OpenCV");
            }

            Mat blob = Dnn.blobFromImage(image, 1.0, new Size(227, 227), 
                                         new Scalar(78.4263377603, 87.7689143744, 114.895847746), 
                                         false, false);

            net.setInput(blob);
            Mat detections = net.forward();

            Core.MinMaxLocResult result = Core.minMaxLoc(detections.reshape(1, 1));
            int classIndex = (int) result.maxLoc.x;

            image.release();
            blob.release();
            detections.release();

            return AGE_BUCKETS[classIndex];

        } catch (Exception e) {
            e.printStackTrace();
            return "Error in processing";
        }
    }
}
EOF

# Controller
echo "Creating AgeController.java..."
cat << 'EOF' > src/main/java/com/example/ageestimation/controller/AgeController.java
package com.example.ageestimation.controller;

import com.example.ageestimation.model.PredictionRecord;
import com.example.ageestimation.service.AgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/age")
@CrossOrigin(origins = "*")
public class AgeController {

    private final AgeService ageService;

    public AgeController(AgeService ageService) {
        this.ageService = ageService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<?> estimateAge(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid image file.");
        }
        try {
            PredictionRecord result = ageService.processImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing image: " + e.getMessage());
        }
    }
}
EOF

echo "Project created successfully in 'age-estimation-backend' folder!"
