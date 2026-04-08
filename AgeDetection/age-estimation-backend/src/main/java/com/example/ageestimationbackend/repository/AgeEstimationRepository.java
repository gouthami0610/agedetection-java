package com.example.ageestimationbackend.repository;

import com.example.ageestimationbackend.model.AgeEstimation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgeEstimationRepository extends JpaRepository<AgeEstimation, Long> {

    List<AgeEstimation> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AgeEstimation> findByPredictedAgeBetweenOrderByTimestampDesc(Integer minAge, Integer maxAge);

    @Query("SELECT a FROM AgeEstimation a ORDER BY a.timestamp DESC LIMIT 10")
    List<AgeEstimation> findLatestEstimations();

    long countByPredictedAge(Integer age);
}
