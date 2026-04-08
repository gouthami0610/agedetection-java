package com.example.ageestimationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.ageestimationbackend")
public class AgeEstimationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgeEstimationBackendApplication.class, args);
    }
}
