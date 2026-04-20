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
public class CategoryTopicController {

    @Autowired
    private CategoryTopicRepo categoryTopicRepo;

    @Autowired
    private ResourceTypeRepo resourceTypeRepo;

    @GetMapping("/categoryTopics")
    public List<CategoryTopic> getAllCategoryTopics() {
        return categoryTopicRepo.findAll();
    }

    @GetMapping("/categoryTopics/active")
    public List<CategoryTopic> getActiveCategoryTopics() {
        return categoryTopicRepo.findByStatus("ACTIVE");
    }

    @PostMapping("/categoryTopic")
    public CategoryTopic createCategoryTopic(@RequestBody CategoryTopic categoryTopic) {

        if (categoryTopic.getCategoryTopic() == null || categoryTopic.getCategoryTopic().trim().isEmpty()) {
            return null;
        }

        String normalizedName = categoryTopic.getCategoryTopic().trim();

        List<CategoryTopic> existingTopics = categoryTopicRepo.findByCategoryTopic(normalizedName);
        if (!existingTopics.isEmpty()) {
            return null;
        }

        List<ResourceType> existingTypes = resourceTypeRepo.findByTypeName(normalizedName);
        if (!existingTypes.isEmpty()) {
            return null;
        }

        categoryTopic.setCategoryTopic(normalizedName);
        categoryTopic.setCreatedAt(LocalDateTime.now());
        categoryTopic.setLastUpdatedAt(LocalDateTime.now());

        if (categoryTopic.getStatus() == null) {
            categoryTopic.setStatus("ACTIVE");
        }

        if (categoryTopic.getUsageCount() == null) {
            categoryTopic.setUsageCount(0);
        }

        return categoryTopicRepo.save(categoryTopic);
    }

    @PutMapping("/categoryTopic/{id}")
    public CategoryTopic updateCategoryTopic(@PathVariable Long id, @RequestBody CategoryTopic newCategoryTopic) {
        CategoryTopic categoryTopic = categoryTopicRepo.findById(id).orElse(null);

        if (categoryTopic == null) {
            return null;
        }

        if (newCategoryTopic.getCategoryTopic() == null || newCategoryTopic.getCategoryTopic().trim().isEmpty()) {
            return null;
        }

        String normalizedName = newCategoryTopic.getCategoryTopic().trim();

        List<CategoryTopic> existingTopics = categoryTopicRepo.findByCategoryTopic(normalizedName);
        for (CategoryTopic existingTopic : existingTopics) {
            if (!existingTopic.getCategoryId().equals(id)) {
                return null;
            }
        }

        List<ResourceType> existingTypes = resourceTypeRepo.findByTypeName(normalizedName);
        if (!existingTypes.isEmpty()) {
            return null;
        }

        categoryTopic.setCategoryTopic(normalizedName);
        categoryTopic.setStatus(newCategoryTopic.getStatus());
        categoryTopic.setLastUpdatedAt(LocalDateTime.now());

        return categoryTopicRepo.save(categoryTopic);
    }

    @PutMapping("/categoryTopic/{id}/deactivate")
    public CategoryTopic deactivateCategoryTopic(@PathVariable Long id) {
        CategoryTopic categoryTopic = categoryTopicRepo.findById(id).orElse(null);

        if (categoryTopic != null) {
            categoryTopic.setStatus("INACTIVE");
            categoryTopic.setLastUpdatedAt(LocalDateTime.now());
            return categoryTopicRepo.save(categoryTopic);
        }

        return null;
    }

    @PutMapping("/categoryTopic/{id}/activate")
    public CategoryTopic activateCategoryTopic(@PathVariable Long id) {
        CategoryTopic categoryTopic = categoryTopicRepo.findById(id).orElse(null);

        if (categoryTopic != null) {
            categoryTopic.setStatus("ACTIVE");
            categoryTopic.setLastUpdatedAt(LocalDateTime.now());
            return categoryTopicRepo.save(categoryTopic);
        }

        return null;
    }
}