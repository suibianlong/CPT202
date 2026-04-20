package cn.mytask.demo.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.mytask.demo.Models.CategoryTopic;
import cn.mytask.demo.Models.ResourceType;
import cn.mytask.demo.Repositories.CategoryTopicRepo;
import cn.mytask.demo.Repositories.ResourceTypeRepo;

@RestController
public class ResourceTypeController {

    @Autowired
    private ResourceTypeRepo resourceTypeRepo;

    @Autowired
    private CategoryTopicRepo categoryTopicRepo;

    @GetMapping("/resourceTypes")
    public List<ResourceType> getAllResourceTypes() {
        return resourceTypeRepo.findAll();
    }

    @GetMapping("/resourceTypes/active")
    public List<ResourceType> getActiveResourceTypes() {
        return resourceTypeRepo.findByStatus("ACTIVE");
    }

    @PostMapping("/resourceType")
    public ResourceType createResourceType(@RequestBody ResourceType resourceType) {

        if (resourceType.getTypeName() == null || resourceType.getTypeName().trim().isEmpty()) {
            return null;
        }

        String normalizedName = resourceType.getTypeName().trim();

        List<ResourceType> existingTypes = resourceTypeRepo.findByTypeName(normalizedName);
        if (!existingTypes.isEmpty()) {
            return null;
        }

        List<CategoryTopic> existingTopics = categoryTopicRepo.findByCategoryTopic(normalizedName);
        if (!existingTopics.isEmpty()) {
            return null;
        }

        resourceType.setTypeName(normalizedName);
        resourceType.setCreatedAt(LocalDateTime.now());
        resourceType.setLastUpdatedAt(LocalDateTime.now());

        if (resourceType.getStatus() == null) {
            resourceType.setStatus("ACTIVE");
        }

        if (resourceType.getUsageCount() == null) {
            resourceType.setUsageCount(0);
        }

        return resourceTypeRepo.save(resourceType);
    }

    @PutMapping("/resourceType/{id}")
    public ResourceType updateResourceType(@PathVariable Long id, @RequestBody ResourceType newResourceType) {
        ResourceType resourceType = resourceTypeRepo.findById(id).orElse(null);

        if (resourceType == null) {
            return null;
        }

        if (newResourceType.getTypeName() == null || newResourceType.getTypeName().trim().isEmpty()) {
            return null;
        }

        String normalizedName = newResourceType.getTypeName().trim();

        List<ResourceType> existingTypes = resourceTypeRepo.findByTypeName(normalizedName);
        for (ResourceType existingType : existingTypes) {
            if (!existingType.getResourceTypeId().equals(id)) {
                return null;
            }
        }

        List<CategoryTopic> existingTopics = categoryTopicRepo.findByCategoryTopic(normalizedName);
        if (!existingTopics.isEmpty()) {
            return null;
        }

        resourceType.setTypeName(normalizedName);
        resourceType.setStatus(newResourceType.getStatus());
        resourceType.setLastUpdatedAt(LocalDateTime.now());

        return resourceTypeRepo.save(resourceType);
    }

    @PutMapping("/resourceType/{id}/deactivate")
    public ResourceType deactivateResourceType(@PathVariable Long id) {
        ResourceType resourceType = resourceTypeRepo.findById(id).orElse(null);

        if (resourceType != null) {
            resourceType.setStatus("INACTIVE");
            resourceType.setLastUpdatedAt(LocalDateTime.now());
            return resourceTypeRepo.save(resourceType);
        }

        return null;
    }

    @PutMapping("/resourceType/{id}/activate")
    public ResourceType activateResourceType(@PathVariable Long id) {
        ResourceType resourceType = resourceTypeRepo.findById(id).orElse(null);

        if (resourceType != null) {
            resourceType.setStatus("ACTIVE");
            resourceType.setLastUpdatedAt(LocalDateTime.now());
            return resourceTypeRepo.save(resourceType);
        }

        return null;
    }
}