package com.cpt202.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttachedFileDTO {
    private Long fileId;
    private String originalFilename;
    private String storedFilename;
    private String fileType;
    private Long fileSize;
    private String uploadedAt;
}
