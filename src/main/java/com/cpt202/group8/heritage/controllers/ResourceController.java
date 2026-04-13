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
        return resourceRepository.findByStatus(ResourceStatus.APPROVED);
    }

    @GetMapping("/{id}")
    public Resource getResourceById(@PathVariable Long id) {
        return resourceRepository.findById(id).orElse(null);
    }

    @GetMapping("/search")
    public List<Resource> searchResources(@RequestParam String keyword) {
        return resourceRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                ResourceStatus.APPROVED, keyword,
                ResourceStatus.APPROVED, keyword
        );
    }

    @GetMapping("/filter")
    public List<Resource> filterResources(@RequestParam Long categoryId) {
        return resourceRepository.findByStatusAndCategoryId(ResourceStatus.APPROVED, categoryId);
    }

    @GetMapping("/sort")
    public List<Resource> sortResources(@RequestParam String by) {
        if ("title".equalsIgnoreCase(by)) {
            return resourceRepository.findByStatusOrderByTitleAsc(ResourceStatus.APPROVED);
        }
        return resourceRepository.findByStatusOrderByReviewedAtDesc(ResourceStatus.APPROVED);
    }

    @GetMapping("/filter-and-sort")
    public List<Resource> filterAndSortResources(@RequestParam Long categoryId,
                                                 @RequestParam String by) {
        if ("title".equalsIgnoreCase(by)) {
            return resourceRepository.findByStatusAndCategoryIdOrderByTitleAsc(
                    ResourceStatus.APPROVED, categoryId
            );
        }
        return resourceRepository.findByStatusAndCategoryIdOrderByReviewedAtDesc(
                ResourceStatus.APPROVED, categoryId
        );
    }
    @GetMapping("/query")
    public List<Resource> queryResources(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String sortBy) {

    List<Resource> resources = resourceRepository.findByStatus(ResourceStatus.APPROVED);

    if (keyword != null && !keyword.trim().isEmpty()) {
        resources = resources.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || r.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    if (categoryId != null) {
        resources = resources.stream()
                .filter(r -> r.getCategoryId().equals(categoryId))
                .toList();
    }

    if ("title".equalsIgnoreCase(sortBy)) {
        resources = resources.stream()
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .toList();
    } else if ("time".equalsIgnoreCase(sortBy)) {
        resources = resources.stream()
                .sorted((a, b) -> b.getReviewedAt().compareTo(a.getReviewedAt()))
                .toList();
    }

    return resources;
}
}