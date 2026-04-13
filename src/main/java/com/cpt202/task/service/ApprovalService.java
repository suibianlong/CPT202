package com.cpt202.task.service;

import com.cpt202.task.entity.ContributorApplication;
import com.cpt202.task.entity.User;
import com.cpt202.task.entity.enumtype.ApprovalStatus;
import com.cpt202.task.entity.enumtype.ContributorStatus;
import com.cpt202.task.repository.ContributorApplicationRepository;
import com.cpt202.task.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalService {

    private final ContributorApplicationRepository contributorApplicationRepository;
    private final UserRepository userRepository;

    public ApprovalService(ContributorApplicationRepository contributorApplicationRepository,
                           UserRepository userRepository) {
        this.contributorApplicationRepository = contributorApplicationRepository;
        this.userRepository = userRepository;
    }

    public List<ContributorApplication> getPendingApplications() {
        return contributorApplicationRepository.findByApprovalStatusOrderBySubmittedAtAsc(ApprovalStatus.PENDING);
    }

    public ContributorApplication getApplicationById(Long applicationId) {
        return contributorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
    }

    @Transactional
    public ContributorApplication approveApplication(Long applicationId, Long reviewerId, String reviewComment) {
        ContributorApplication application = getApplicationById(applicationId);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));

        application.setApprovalStatus(ApprovalStatus.APPROVED);
        application.setReviewer(reviewer);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewComment(reviewComment);

        User applicant = application.getUser();
        applicant.setContributorStatus(ContributorStatus.APPROVED);
        applicant.setIsContributor(true);
        userRepository.save(applicant);

        return contributorApplicationRepository.save(application);
    }

    @Transactional
    public ContributorApplication rejectApplication(Long applicationId, Long reviewerId, String reviewComment) {
        ContributorApplication application = getApplicationById(applicationId);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));

        application.setApprovalStatus(ApprovalStatus.REJECTED);
        application.setReviewer(reviewer);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewComment(reviewComment);

        User applicant = application.getUser();
        applicant.setContributorStatus(ContributorStatus.REJECTED);
        applicant.setIsContributor(false);
        userRepository.save(applicant);

        return contributorApplicationRepository.save(application);
    }
}
