package com.example.ageestimationbackend.controller;

import com.example.ageestimationbackend.dto.AgeEstimationResponse;
import com.example.ageestimationbackend.model.AgeEstimation;
import com.example.ageestimationbackend.service.AgeEstimationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/estimations")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgeEstimationController {

    @Autowired
    private AgeEstimationService ageEstimationService;

    /**
     * Upload image and estimate age
     */
    @PostMapping("/upload")
    public ResponseEntity<AgeEstimationResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Received image upload request: {}", file.getOriginalFilename());

        try {
            AgeEstimation estimation = ageEstimationService.processImage(file);

            AgeEstimationResponse response = buildResponse(estimation, true, "Age estimation completed successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error processing image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AgeEstimationResponse(null, null, null, null, null, null, null, null,
                            "Error processing image: " + e.getMessage(), false));

        } catch (IllegalArgumentException e) {
            log.error("Invalid image file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AgeEstimationResponse(null, null, null, null, null, null, null, null,
                            "Invalid image file: " + e.getMessage(), false));

        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AgeEstimationResponse(null, null, null, null, null, null, null, null,
                            "Unexpected error: " + e.getMessage(), false));
        }
    }

    /**
     * Get all estimations
     */
    @GetMapping("/all")
    public ResponseEntity<List<AgeEstimation>> getAllEstimations() {
        log.info("Fetching all estimations");
        List<AgeEstimation> estimations = ageEstimationService.getAllEstimations();
        return ResponseEntity.ok(estimations);
    }

    /**
     * Get estimation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEstimationById(@PathVariable Long id) {
        log.info("Fetching estimation with ID: {}", id);
        AgeEstimation estimation = ageEstimationService.getEstimationById(id);

        if (estimation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Estimation not found with ID: " + id);
        }

        return ResponseEntity.ok(estimation);
    }

    /**
     * Get recent estimations
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AgeEstimation>> getRecentEstimations() {
        log.info("Fetching recent estimations");
        List<AgeEstimation> estimations = ageEstimationService.getRecentEstimations();
        return ResponseEntity.ok(estimations);
    }

    /**
     * Get estimations by age range
     */
    @GetMapping("/range")
    public ResponseEntity<List<AgeEstimation>> getEstimationsByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge) {
        log.info("Fetching estimations between age {} and {}", minAge, maxAge);

        if (minAge > maxAge) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<AgeEstimation> estimations = ageEstimationService.getEstimationsByAgeRange(minAge, maxAge);
        return ResponseEntity.ok(estimations);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats/count/{age}")
    public ResponseEntity<?> getEstimationCountByAge(@PathVariable Integer age) {
        log.info("Fetching count for age: {}", age);
        long count = ageEstimationService.getEstimationCountByAge(age);
        return ResponseEntity.ok(count);
    }

    /**
     * Delete estimation by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEstimation(@PathVariable Long id) {
        log.info("Deleting estimation with ID: {}", id);

        AgeEstimation estimation = ageEstimationService.getEstimationById(id);
        if (estimation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Estimation not found with ID: " + id);
        }

        ageEstimationService.deleteEstimation(id);
        return ResponseEntity.ok("Estimation deleted successfully");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Age Estimation Backend is running");
    }

    /**
     * Build response DTO
     */
    private AgeEstimationResponse buildResponse(AgeEstimation estimation, boolean success, String message) {
        return new AgeEstimationResponse(
                estimation.getId(),
                estimation.getImagePath(),
                estimation.getPredictedAge(),
                estimation.getConfidence(),
                estimation.getFileName(),
                estimation.getImageSize(),
                estimation.getProcessingTimeMs(),
                estimation.getTimestamp(),
                message,
                success
        );
    }
}
