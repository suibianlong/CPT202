package org.example.cpt202music.model.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String categoryType;
    private String categoryTopic;
    private String description;
}
