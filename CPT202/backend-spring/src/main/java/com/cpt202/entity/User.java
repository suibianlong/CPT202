package com.cpt202.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType role = RoleType.USER;

    @Column(name = "is_contributor", nullable = false)
    private Boolean isContributor = false;

    @Column(name = "last_updated_time", nullable = false)
    private LocalDateTime lastUpdatedTime;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ContributorApproval contributorApproval;

    @OneToMany(mappedBy = "contributor", cascade = CascadeType.ALL)
    private Set<Resource> resources = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        lastUpdatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedTime = LocalDateTime.now();
    }

    public enum RoleType {
        USER, REVIEWER, ADMIN
    }
}
