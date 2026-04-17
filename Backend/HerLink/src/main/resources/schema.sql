DROP TABLE IF EXISTS review_record;
DROP TABLE IF EXISTS resource_tag;
DROP TABLE IF EXISTS resource_submission;
DROP TABLE IF EXISTS resource;
DROP TABLE IF EXISTS contributor_request;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

CREATE TABLE contributor_request (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP NULL,
    reviewed_by BIGINT NULL,
    review_comment VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_contributor_request_user FOREIGN KEY (user_id) REFERENCES app_user(user_id),
    CONSTRAINT fk_contributor_request_reviewer FOREIGN KEY (reviewed_by) REFERENCES app_user(user_id)
);

CREATE TABLE category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_type VARCHAR(50) NOT NULL,
    category_topic VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP NOT NULL
);

CREATE TABLE tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP NOT NULL
);

CREATE TABLE resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contributor_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category_id BIGINT NOT NULL,
    place VARCHAR(255) NULL,
    preview_image VARCHAR(255) NULL,
    media_url VARCHAR(255) NULL,
    status VARCHAR(50) NOT NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NULL,
    resource_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_resource_contributor FOREIGN KEY (contributor_id) REFERENCES app_user(user_id),
    CONSTRAINT fk_resource_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE resource_submission (
    submission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    submitted_by BIGINT NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    submission_note VARCHAR(500) NULL,
    status_snapshot VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_resource_submission_resource FOREIGN KEY (resource_id) REFERENCES resource(id),
    CONSTRAINT fk_resource_submission_submitter FOREIGN KEY (submitted_by) REFERENCES app_user(user_id)
);

CREATE TABLE resource_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    CONSTRAINT fk_resource_tag_resource FOREIGN KEY (resource_id) REFERENCES resource(id),
    CONSTRAINT fk_resource_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(tag_id),
    CONSTRAINT uk_resource_tag UNIQUE (resource_id, tag_id)
);

CREATE TABLE review_record (
    review_record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    submission_id BIGINT NULL,
    version_no INT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action_description VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    feedback_comment VARCHAR(500) NULL,
    reviewed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_review_record_resource FOREIGN KEY (resource_id) REFERENCES resource(id),
    CONSTRAINT fk_review_record_submission FOREIGN KEY (submission_id) REFERENCES resource_submission(submission_id),
    CONSTRAINT fk_review_record_reviewer FOREIGN KEY (reviewer_id) REFERENCES app_user(user_id)
);
