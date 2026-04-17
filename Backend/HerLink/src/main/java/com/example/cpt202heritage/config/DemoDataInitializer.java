package com.example.cpt202heritage.config;

import com.example.cpt202heritage.enums.ContributorApplicationStatusEnum;
import com.example.cpt202heritage.enums.ResourceStatusEnum;
import com.example.cpt202heritage.enums.UserRoleEnum;
import com.example.cpt202heritage.util.PasswordHashService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DemoDataInitializer {

    @Bean
    public ApplicationRunner seedDemoData(JdbcTemplate jdbcTemplate,
                                          PasswordHashService passwordHashService,
                                          @Value("${module3.demo-data-enabled:true}") boolean demoDataEnabled) {
        return arguments -> {
            if (!demoDataEnabled) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            Timestamp currentTimestamp = Timestamp.valueOf(now);
            Timestamp earlierTimestamp = Timestamp.valueOf(now.minusDays(12));
            Timestamp approvedTimestamp = Timestamp.valueOf(now.minusDays(8));
            Timestamp draftTimestamp = Timestamp.valueOf(now.minusDays(2));
            Timestamp pendingTimestamp = Timestamp.valueOf(now.minusHours(20));

            jdbcTemplate.update(
                    "INSERT INTO category (category_id, category_type, category_topic, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    1L, "IMAGE", "Architecture", "ACTIVE", 28, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO category (category_id, category_type, category_topic, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    2L, "VIDEO", "Tradition", "ACTIVE", 21, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO category (category_id, category_type, category_topic, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    3L, "IMAGE", "Artifact", "ACTIVE", 17, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO category (category_id, category_type, category_topic, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    4L, "DOCUMENT", "Place", "ACTIVE", 14, earlierTimestamp, currentTimestamp
            );

            jdbcTemplate.update(
                    "INSERT INTO tag (tag_id, tag_name, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?)",
                    1L, "Temple", "ACTIVE", 19, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO tag (tag_id, tag_name, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?)",
                    2L, "Local Memory", "ACTIVE", 15, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO tag (tag_id, tag_name, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?)",
                    3L, "Festival", "ACTIVE", 13, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO tag (tag_id, tag_name, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?)",
                    4L, "Village", "ACTIVE", 10, earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO tag (tag_id, tag_name, status, usage_count, created_time, last_updated_time) VALUES (?, ?, ?, ?, ?, ?)",
                    5L, "Oral History", "ACTIVE", 8, earlierTimestamp, currentTimestamp
            );

            jdbcTemplate.update(
                    "INSERT INTO app_user (user_id, name, email, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    1L, "System Admin", "admin@heritage.local", passwordHashService.hash("Admin123!"),
                    UserRoleEnum.ADMINISTRATOR.getValue(), earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO app_user (user_id, name, email, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    2L, "Approved Contributor", "contributor@heritage.local", passwordHashService.hash("Contributor123!"),
                    UserRoleEnum.REGISTERED_VIEWER.getValue(), earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO app_user (user_id, name, email, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    3L, "Registered Viewer", "viewer@heritage.local", passwordHashService.hash("Viewer123!"),
                    UserRoleEnum.REGISTERED_VIEWER.getValue(), earlierTimestamp, currentTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO app_user (user_id, name, email, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    4L, "Pending Applicant", "pending@heritage.local", passwordHashService.hash("Pending123!"),
                    UserRoleEnum.REGISTERED_VIEWER.getValue(), earlierTimestamp, currentTimestamp
            );

            jdbcTemplate.update(
                    "INSERT INTO contributor_request (request_id, user_id, status, requested_at, reviewed_at, reviewed_by, review_comment, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    1L, 2L, ContributorApplicationStatusEnum.APPROVED.getValue(),
                    Timestamp.valueOf(now.minusDays(9)), approvedTimestamp, 1L,
                    "The applicant can now create and submit resources.", Timestamp.valueOf(now.minusDays(9)), approvedTimestamp
            );
            jdbcTemplate.update(
                    "INSERT INTO contributor_request (request_id, user_id, status, requested_at, reviewed_at, reviewed_by, review_comment, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    2L, 4L, ContributorApplicationStatusEnum.PENDING.getValue(),
                    Timestamp.valueOf(now.minusDays(1)), null, null,
                    null, Timestamp.valueOf(now.minusDays(1)), Timestamp.valueOf(now.minusDays(1))
            );

            jdbcTemplate.update(
                    "INSERT INTO resource (id, contributor_id, title, description, category_id, place, preview_image, media_url, status, reviewed_at, created_at, updated_at, archived_at, resource_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    1001L, 2L, "Traditional Temple Entrance",
                    "Stone gateway details collected from the old village temple.",
                    1L, "Suzhou", "resource-1001/temple-cover.jpg", "resource-1001/temple.jpg",
                    ResourceStatusEnum.DRAFT.getValue(), null, earlierTimestamp, draftTimestamp, null, "IMAGE"
            );
            jdbcTemplate.update(
                    "INSERT INTO resource (id, contributor_id, title, description, category_id, place, preview_image, media_url, status, reviewed_at, created_at, updated_at, archived_at, resource_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    1002L, 2L, "Festival Parade Recording",
                    "Video footage of the annual community heritage parade.",
                    2L, "Hangzhou", "resource-1002/festival-cover.jpg", "resource-1002/festival.mp4",
                    ResourceStatusEnum.PENDING_REVIEW.getValue(), null, earlierTimestamp, pendingTimestamp, null, "VIDEO"
            );

            jdbcTemplate.update(
                    "INSERT INTO resource_submission (submission_id, resource_id, version_no, submitted_by, submitted_at, submission_note, status_snapshot, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    1L, 1002L, 1, 2L, pendingTimestamp,
                    "First review submission for the festival archive.", ResourceStatusEnum.PENDING_REVIEW.getValue(), pendingTimestamp
            );

            jdbcTemplate.update(
                    "INSERT INTO resource_tag (id, resource_id, tag_id) VALUES (?, ?, ?)",
                    1L, 1001L, 1L
            );
            jdbcTemplate.update(
                    "INSERT INTO resource_tag (id, resource_id, tag_id) VALUES (?, ?, ?)",
                    2L, 1001L, 2L
            );
            jdbcTemplate.update(
                    "INSERT INTO resource_tag (id, resource_id, tag_id) VALUES (?, ?, ?)",
                    3L, 1002L, 3L
            );
            jdbcTemplate.update(
                    "INSERT INTO resource_tag (id, resource_id, tag_id) VALUES (?, ?, ?)",
                    4L, 1002L, 4L
            );
        };
    }
}
