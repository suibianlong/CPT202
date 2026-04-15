-- =============================================
-- CPT202 Database Schema
-- Module #4 - Contributor Module
-- =============================================

-- =============================================
-- 1. Users Table
-- Records user information
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    bio TEXT NULL DEFAULT NULL,
    role ENUM('user', 'reviewer', 'admin') NOT NULL DEFAULT 'user',
    is_contributor TINYINT(1) NOT NULL DEFAULT 0,
    last_updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 2. Roles Table
-- Records role information
-- =============================================
CREATE TABLE IF NOT EXISTS roles (
    role_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. User_Role Table
-- Records user-role relationships
-- =============================================
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 4. Contributor_approvals Table
-- Records contributor application information
-- =============================================
CREATE TABLE IF NOT EXISTS contributor_approvals (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    application_reason TEXT NULL DEFAULT NULL,
    approval_status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT NULL DEFAULT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    review_comment TEXT NULL DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_application (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 5. Categories Table
-- Records resource category information
-- =============================================
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_type VARCHAR(20) NOT NULL COMMENT '分类类型：视频/图片等',
    category_topic VARCHAR(20) NOT NULL COMMENT '分类主题：Object/Place等',
    description TEXT NULL DEFAULT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    usage_count INT NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_type_topic (category_type, category_topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 6. Tags Table
-- Records tag information
-- =============================================
CREATE TABLE IF NOT EXISTS tags (
    tag_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(100) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    usage_count INT NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 7. Resources Table
-- Records resource detailed information
-- =============================================
CREATE TABLE IF NOT EXISTS resources (
    resource_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contributor_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    place VARCHAR(255) NULL DEFAULT NULL,
    preview_image VARCHAR(500) NULL DEFAULT NULL,
    type ENUM('video', 'image', 'document', 'audio') NULL DEFAULT NULL,
    status ENUM('Draft', 'Pending Review', 'Rejected', 'Approved', 'Archived') NOT NULL DEFAULT 'Draft',
    approved_at TIMESTAMP NULL DEFAULT NULL,
    archived_at TIMESTAMP NULL DEFAULT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_submitted_time TIMESTAMP NULL DEFAULT NULL,
    last_published_time TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (contributor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_title (title),
    INDEX idx_status (status),
    INDEX idx_contributor_status (contributor_id, status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 8. Resource_Category Table
-- Records resource-category relationships
-- =============================================
CREATE TABLE IF NOT EXISTS resource_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    UNIQUE KEY uk_resource_category (resource_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 9. Resource_Tag Table
-- Records resource-tag relationships
-- =============================================
CREATE TABLE IF NOT EXISTS resource_tag (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
    UNIQUE KEY uk_resource_tag (resource_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 10. Attached_file Table
-- Records resource attached files
-- =============================================
CREATE TABLE IF NOT EXISTS attached_file (
    file_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NULL DEFAULT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    UNIQUE KEY uk_stored_filename (stored_filename),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 11. Resource_Submission Table
-- Records each submission version of a resource
-- =============================================
CREATE TABLE IF NOT EXISTS resource_submission (
    submission_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    submitted_by BIGINT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submission_note TEXT NULL DEFAULT NULL,
    status_snapshot ENUM('Draft', 'Pending Review', 'Rejected', 'Approved', 'Archived') NOT NULL DEFAULT 'Pending Review',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (submitted_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_resource_id (resource_id),
    INDEX idx_submitted_by (submitted_by),
    UNIQUE KEY uk_resource_version (resource_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 12. Review_records Table
-- Records each review history
-- =============================================
CREATE TABLE IF NOT EXISTS review_records (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    submission_id BIGINT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    reviewer_id BIGINT NOT NULL,
    action_description ENUM('APPROVE', 'REJECT') NOT NULL,
    status ENUM('APPROVED', 'REJECTED') NOT NULL,
    feedback_comment TEXT NULL DEFAULT NULL,
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (submission_id) REFERENCES resource_submission(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_resource_id (resource_id),
    INDEX idx_reviewer_id (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 13. Feedback Table
-- Records feedback files uploaded during review
-- =============================================
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    review_record_id BIGINT NULL DEFAULT NULL,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NULL DEFAULT NULL,
    file_type VARCHAR(50) NULL DEFAULT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_record_id) REFERENCES review_records(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 14. Comments Table
-- Records user comments on resources
-- =============================================
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Initial Data - Default Roles
-- =============================================
INSERT INTO roles (role_name, description) VALUES
    ('admin', 'System administrator with full access'),
    ('contributor', 'Registered contributor who can submit resources'),
    ('reviewer', 'Content reviewer who can approve/reject resources'),
    ('user', 'Regular user with basic access')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- =============================================
-- Initial Data - Default Categories
-- =============================================
INSERT INTO categories (category_type, category_topic, description) VALUES
    ('image', 'Place', 'Geographic locations, landmarks, heritage sites'),
    ('image', 'Tradition', 'Cultural traditions, customs, rituals'),
    ('image', 'Story', 'Legends, myths, historical narratives'),
    ('image', 'Object', 'Cultural artifacts, objects, crafts'),
    ('image', 'Material', 'Materials, substances, natural resources')
ON DUPLICATE KEY UPDATE description = VALUES(description);
