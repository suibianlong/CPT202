package com.cpt202.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String categoryType;
    private String categoryTopic;
    private String description;
}
