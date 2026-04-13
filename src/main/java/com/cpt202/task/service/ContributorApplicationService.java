package com.cpt202.task.service;

import com.cpt202.task.entity.ContributorApplication;
import com.cpt202.task.entity.User;
import com.cpt202.task.entity.enumtype.ApprovalStatus;
import com.cpt202.task.entity.enumtype.ContributorStatus;
import com.cpt202.task.repository.ContributorApplicationRepository;
import com.cpt202.task.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ContributorApplicationService {

    private final ContributorApplicationRepository contributorApplicationRepository;
    private final UserRepository userRepository;

    public ContributorApplicationService(ContributorApplicationRepository contributorApplicationRepository,
                                         UserRepository userRepository) {
        this.contributorApplicationRepository = contributorApplicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContributorApplication createApplication(Long userId, String applicationReason) {
        if (contributorApplicationRepository.existsByUserUserIdAndApprovalStatus(userId, ApprovalStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending contributor application.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ContributorApplication application = new ContributorApplication();
        application.setUser(user);
        application.setApplicationReason(applicationReason);
        application.setApprovalStatus(ApprovalStatus.PENDING);

        user.setContributorStatus(ContributorStatus.PENDING);
        user.setIsContributor(false);
        userRepository.save(user);

        return contributorApplicationRepository.save(application);
    }

    public ContributorApplication getLatestApplication(Long userId) {
        return contributorApplicationRepository.findFirstByUserUserIdOrderBySubmittedAtDesc(userId).orElse(null);
    }
}
