package com.cpt202.reviewworkflow.dto;

import java.time.LocalDateTime;

public record ResourceSection(
    String title,
    String description,
    String place,
    String resourceType,
    String previewImage,
    String mediaUrl,
    String copyrightDeclaration,
    String usageDeclaration,
    boolean visibleToUsers,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime reviewedAt
) {
}
