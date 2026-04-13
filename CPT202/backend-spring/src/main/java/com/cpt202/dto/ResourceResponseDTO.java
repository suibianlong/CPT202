package com.cpt202.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResourceResponseDTO {
    private Long resourceId;
    private String title;
    private String description;
    private String place;
    private String status;
    private String createdTime;
    private String lastUpdatedTime;
    private String lastSubmittedTime;
    private String lastPublishedTime;
    private List<CategoryDTO> categories;
    private List<TagDTO> tags;
    private List<AttachedFileDTO> attachedFiles;
}
