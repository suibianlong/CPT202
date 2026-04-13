package com.cpt202.review.dto;

import java.time.LocalDateTime;

public record ResourceSection(
        String title,
        String description,
        String place,
        String resourceType,
        String previewImage,
        String mediaUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime reviewedAt
) {
}
