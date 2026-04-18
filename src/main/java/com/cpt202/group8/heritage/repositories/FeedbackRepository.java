package com.cpt202.group8.heritage.repositories;

import com.cpt202.group8.heritage.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUserIdOrderByUploadedAtDesc(Long userId);
}