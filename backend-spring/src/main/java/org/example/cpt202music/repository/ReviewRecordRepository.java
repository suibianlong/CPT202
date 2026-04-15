package org.example.cpt202music.repository;

import org.example.cpt202music.model.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
    List<ReviewRecord> findByResourceResourceId(Long resourceId);
    List<ReviewRecord> findByReviewerUserId(Long reviewerId);
}
