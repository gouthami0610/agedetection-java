package com.example.ageestimationbackend.service;

import com.example.ageestimationbackend.model.AgeEstimation;
import com.example.ageestimationbackend.repository.AgeEstimationRepository;
import com.example.ageestimationbackend.config.FileUploadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AgeEstimationService {

    @Autowired
    private AgeEstimationRepository ageEstimationRepository;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Autowired
    private OpenCVService openCVService;

    /**
     * Process image and estimate age
     */
    public AgeEstimation processImage(MultipartFile imageFile) throws IOException {
        log.info("Processing image: {}", imageFile.getOriginalFilename());

        // Validate file
        validateImageFile(imageFile);

        // Save file
        String filePath = saveImageFile(imageFile);
        log.info("Image saved at: {}", filePath);

        // Process with OpenCV and get age estimation
        long startTime = System.currentTimeMillis();
        int predictedAge = openCVService.estimateAge(filePath);
        double confidence = openCVService.getConfidence(filePath);
        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Age estimated: {} with confidence: {}", predictedAge, confidence);

        // Create and save AgeEstimation record
        AgeEstimation estimation = new AgeEstimation();
        estimation.setImagePath(filePath);
        estimation.setPredictedAge(predictedAge);
        estimation.setConfidence(confidence);
        estimation.setFileName(imageFile.getOriginalFilename());
        estimation.setImageSize(imageFile.getSize());
        estimation.setProcessingTimeMs(processingTime);
        estimation.setTimestamp(LocalDateTime.now());

        AgeEstimation savedEstimation = ageEstimationRepository.save(estimation);
        log.info("Age estimation saved with ID: {}", savedEstimation.getId());

        return savedEstimation;
    }

    /**
     * Get all estimations
     */
    public List<AgeEstimation> getAllEstimations() {
        return ageEstimationRepository.findAll();
    }

    /**
     * Get estimation by ID
     */
    public AgeEstimation getEstimationById(Long id) {
        return ageEstimationRepository.findById(id).orElse(null);
    }

    /**
     * Get recent estimations
     */
    public List<AgeEstimation> getRecentEstimations() {
        return ageEstimationRepository.findLatestEstimations();
    }

    /**
     * Get estimations by age range
     */
    public List<AgeEstimation> getEstimationsByAgeRange(Integer minAge, Integer maxAge) {
        return ageEstimationRepository.findByPredictedAgeBetweenOrderByTimestampDesc(minAge, maxAge);
    }

    /**
     * Get statistics
     */
    public long getEstimationCountByAge(Integer age) {
        return ageEstimationRepository.countByPredictedAge(age);
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!fileUploadConfig.isValidFileSize(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        log.info("Image file validation passed: {}", file.getOriginalFilename());
    }

    /**
     * Save image file to disk
     */
    private String saveImageFile(MultipartFile file) throws IOException {
        String uploadDir = fileUploadConfig.getUploadDir();
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        return filePath.toString();
    }

    /**
     * Generate unique file name
     */
    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return uuid + extension;
    }

    /**
     * Delete estimation by ID
     */
    public void deleteEstimation(Long id) {
        AgeEstimation estimation = getEstimationById(id);
        if (estimation != null) {
            try {
                Files.deleteIfExists(Paths.get(estimation.getImagePath()));
            } catch (IOException e) {
                log.error("Error deleting image file: {}", estimation.getImagePath(), e);
            }
            ageEstimationRepository.deleteById(id);
            log.info("Estimation deleted with ID: {}", id);
        }
    }
}
