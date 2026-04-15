package org.example.cpt202music.repository;

import org.example.cpt202music.model.entity.ContributorApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorApprovalRepository extends JpaRepository<ContributorApproval, Long> {
    Optional<ContributorApproval> findByUserUserId(Long userId);
    List<ContributorApproval> findByApprovalStatus(ContributorApproval.ApprovalStatus status);
}
