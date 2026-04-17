package com.example.cpt202heritage.service;

import com.example.cpt202heritage.entity.ContributorApplication;
import com.example.cpt202heritage.entity.User;
import com.example.cpt202heritage.enums.ApprovalStatus;
import com.example.cpt202heritage.enums.ContributorStatus;
import com.example.cpt202heritage.repository.ContributorApplicationRepository;
import com.example.cpt202heritage.repository.UserRepository;
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
