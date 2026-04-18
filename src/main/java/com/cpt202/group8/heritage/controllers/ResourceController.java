package com.cpt202.group8.heritage.controllers;

import com.cpt202.group8.heritage.entities.Resource;
import com.cpt202.group8.heritage.entities.ResourceStatus;
import com.cpt202.group8.heritage.repositories.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    @GetMapping
    public List<Resource> getApprovedResources() {
        return resourceRepository.findByStatus(ResourceStatus.Approved);
    }

    @GetMapping("/{id}")
    public Resource getResourceById(@PathVariable Long id) {
        return resourceRepository.findById(id).orElse(null);
    }

    @GetMapping("/search")
    public List<Resource> searchResources(@RequestParam String keyword) {
        return resourceRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                ResourceStatus.Approved, keyword,
                ResourceStatus.Approved, keyword
        );
    }

    @GetMapping("/sort")
    public List<Resource> sortResources(@RequestParam String by) {
        if ("title".equalsIgnoreCase(by)) {
            return resourceRepository.findByStatusOrderByTitleAsc(ResourceStatus.Approved);
        }
        return resourceRepository.findByStatusOrderByReviewedAtDesc(ResourceStatus.Approved);
    }

    @GetMapping("/query")
    public List<Resource> queryResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String sortBy) {

        List<Resource> resources = resourceRepository.findByStatus(ResourceStatus.Approved);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            resources = resources.stream()
                    .filter(r -> (r.getTitle() != null && r.getTitle().toLowerCase().contains(lowerKeyword))
                            || (r.getDescription() != null && r.getDescription().toLowerCase().contains(lowerKeyword)))
                    .toList();
        }

        if (type != null && !type.trim().isEmpty()) {
            resources = resources.stream()
                    .filter(r -> r.getResourceType() != null && r.getResourceType().equalsIgnoreCase(type))
                    .toList();
        }

        if (topic != null && !topic.trim().isEmpty()) {
            try {
                Long categoryId = Long.parseLong(topic);
                resources = resources.stream()
                        .filter(r -> r.getCategoryId() != null && r.getCategoryId().equals(categoryId))
                        .toList();
            } catch (NumberFormatException e) {
                return List.of();
            }
        }

        if ("title".equalsIgnoreCase(sortBy)) {
            resources = resources.stream()
                    .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                    .toList();
        } else if ("time".equalsIgnoreCase(sortBy)) {
            resources = resources.stream()
                    .sorted((a, b) -> {
                        if (a.getReviewedAt() == null && b.getReviewedAt() == null) return 0;
                        if (a.getReviewedAt() == null) return 1;
                        if (b.getReviewedAt() == null) return -1;
                        return b.getReviewedAt().compareTo(a.getReviewedAt());
                    })
                    .toList();
        }

        return resources;
    }
}