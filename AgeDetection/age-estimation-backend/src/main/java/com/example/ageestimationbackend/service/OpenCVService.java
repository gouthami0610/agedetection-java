package com.example.ageestimationbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class OpenCVService {

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            log.info("OpenCV library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            log.error("Failed to load OpenCV library: {}", e.getMessage());
        }
    }

    private double confidence = 0.0;

    /**
     * Estimate age from image using face detection and age estimation
     */
    public int estimateAge(String imagePath) {
        try {
            Mat image = Imgcodecs.imread(imagePath);

            if (image.empty()) {
                log.error("Failed to load image: {}", imagePath);
                return -1;
            }

            // Preprocess image
            Mat processedImage = preprocessImage(image);

            // Detect faces
            Mat faceDetected = detectFaces(processedImage);

            // Estimate age from face
            int estimatedAge = performAgeEstimation(faceDetected);

            image.release();
            processedImage.release();
            faceDetected.release();

            log.info("Age estimation completed: {}", estimatedAge);
            return estimatedAge;

        } catch (Exception e) {
            log.error("Error during age estimation: {}", e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Preprocess image for better detection
     */
    private Mat preprocessImage(Mat image) {
        Mat processed = new Mat();
        
        // Resize image if too large
        if (image.width() > 800 || image.height() > 600) {
            Size newSize = new Size(800, 600);
            Imgproc.resize(image, processed, newSize);
        } else {
            processed = image.clone();
        }

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(processed, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply histogram equalization
        Imgproc.equalizeHist(gray, gray);

        log.debug("Image preprocessing completed");
        return gray;
    }

    /**
     * Detect faces in image
     */
    private Mat detectFaces(Mat image) {
        Mat detectedFaces = image.clone();
        try {
            // Simple face detection simulation using edge detection
            Mat edges = new Mat();
            Imgproc.Canny(image, edges, 50, 150);
            detectedFaces = edges;
            log.debug("Face detection completed");
        } catch (Exception e) {
            log.error("Error during face detection: {}", e.getMessage());
        }
        return detectedFaces;
    }

    /**
     * Perform age estimation using image analysis
     * This is a simulated estimation based on image properties
     */
    private int performAgeEstimation(Mat faceImage) {
        try {
            // Calculate image statistics for age estimation
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stdDev = new MatOfDouble();
            Core.meanStdDev(faceImage, mean, stdDev);

            double[] meanValues = mean.toArray();
            double[] stdDevValues = stdDev.toArray();

            // Simulate age estimation based on image characteristics
            double brightness = meanValues[0];
            double variance = stdDevValues[0];

            // Age estimation algorithm (simulated)
            int estimatedAge = estimateAgeFromFeatures(brightness, variance);

            // Set confidence score
            this.confidence = calculateConfidence(brightness, variance);

            log.debug("Age estimation: {}, Confidence: {}", estimatedAge, this.confidence);
            return estimatedAge;

        } catch (Exception e) {
            log.error("Error in age estimation algorithm: {}", e.getMessage());
            return 25; // Default age
        }
    }

    /**
     * Estimate age based on image features
     */
    private int estimateAgeFromFeatures(double brightness, double variance) {
        // Simple heuristic-based age estimation
        int baseAge = 25;
        
        // Adjust based on brightness (darker = older)
        if (brightness < 50) {
            baseAge += 10;
        } else if (brightness < 100) {
            baseAge += 5;
        }

        // Adjust based on variance (high variance could indicate more details/wrinkles)
        if (variance > 50) {
            baseAge += 8;
        } else if (variance > 30) {
            baseAge += 3;
        }

        // Ensure age is within reasonable bounds
        return Math.max(1, Math.min(100, baseAge));
    }

    /**
     * Calculate confidence score for age estimation
     */
    private double calculateConfidence(double brightness, double variance) {
        // Confidence based on image quality indicators
        double confidenceScore = 0.5;

        // Adjust confidence based on variance (more features = higher confidence)
        if (variance > 40) {
            confidenceScore += 0.3;
        } else if (variance > 20) {
            confidenceScore += 0.2;
        }

        // Adjust based on brightness (optimal brightness improves confidence)
        if (brightness > 40 && brightness < 120) {
            confidenceScore += 0.2;
        }

        return Math.min(1.0, confidenceScore);
    }

    /**
     * Get confidence score from last estimation
     */
    public double getConfidence(String imagePath) {
        return this.confidence;
    }

    /**
     * Validate if image file exists
     */
    public boolean validateImageFile(String imagePath) {
        File file = new File(imagePath);
        return file.exists() && file.isFile();
    }
}
