package com.cpt202.task.repository;

import com.cpt202.task.entity.ContributorApplication;
import com.cpt202.task.entity.enumtype.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorApplicationRepository extends JpaRepository<ContributorApplication, Long> {

    boolean existsByUserUserIdAndApprovalStatus(Long userId, ApprovalStatus approvalStatus);

    Optional<ContributorApplication> findFirstByUserUserIdOrderBySubmittedAtDesc(Long userId);

    List<ContributorApplication> findByApprovalStatusOrderBySubmittedAtAsc(ApprovalStatus approvalStatus);
}
