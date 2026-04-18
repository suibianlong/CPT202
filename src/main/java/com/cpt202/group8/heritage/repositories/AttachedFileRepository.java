package com.cpt202.group8.heritage.repositories;

import com.cpt202.group8.heritage.entities.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {
    List<AttachedFile> findByFeedback_FeedbackId(Long feedbackId);
}