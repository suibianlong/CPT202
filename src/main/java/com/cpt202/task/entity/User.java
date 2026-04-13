package com.cpt202.task.entity;

import com.cpt202.task.entity.enumtype.ContributorStatus;
import com.cpt202.task.entity.enumtype.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "last_updated_time", nullable = false)
    private LocalDateTime lastUpdatedTime;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "is_contributor", nullable = false)
    private Boolean isContributor = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "contributor_status", nullable = false, length = 20)
    private ContributorStatus contributorStatus = ContributorStatus.NOT_APPLIED;

    @Column(name = "account_status", length = 20)
    private String accountStatus;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        lastUpdatedTime = now;
        if (isContributor == null) {
            isContributor = false;
        }
        if (role == null) {
            role = Role.USER;
        }
        if (contributorStatus == null) {
            contributorStatus = ContributorStatus.NOT_APPLIED;
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastUpdatedTime = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Boolean getIsContributor() {
        return isContributor;
    }

    public void setIsContributor(Boolean contributor) {
        isContributor = contributor;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ContributorStatus getContributorStatus() {
        return contributorStatus;
    }

    public void setContributorStatus(ContributorStatus contributorStatus) {
        this.contributorStatus = contributorStatus;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
