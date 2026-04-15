package org.example.cpt202music.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ResourceCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 255, message = "Place must be less than 255 characters")
    private String place;

    private String copyrightDeclaration;

    private String usageDeclaration;

    private List<Long> categoryIds;

    private List<String> tagNames;
}
