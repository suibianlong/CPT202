package cn.mytask.demo.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.mytask.demo.Models.Tag;
import cn.mytask.demo.Repositories.TagRepo;

@RestController
public class TagController {

    @Autowired
    private TagRepo tagRepo;

    @GetMapping("/tags")
    public List<Tag> getAllTags() {
        return tagRepo.findAll();
    }

    @GetMapping("/tags/active")
    public List<Tag> getActiveTags() {
        return tagRepo.findByStatus("ACTIVE");
    }

    @PutMapping("/tag/{id}")
    public Tag updateTag(@PathVariable Long id, @RequestBody Tag newTag) {
        Tag tag = tagRepo.findById(id).orElse(null);

        if (tag == null) {
            return null;
        }

        if (newTag.getTagName() == null || newTag.getTagName().trim().isEmpty()) {
            return null;
        }

        String normalizedName = newTag.getTagName().trim();

        List<Tag> existingTags = tagRepo.findByTagName(normalizedName);

        for (Tag existingTag : existingTags) {
            if (!existingTag.getTagId().equals(id)) {
                return null;
            }
        }

        tag.setTagName(normalizedName);
        tag.setStatus(newTag.getStatus());
        tag.setLastUpdatedAt(LocalDateTime.now());

        return tagRepo.save(tag);
    }

    @PutMapping("/tag/{id}/deactivate")
    public Tag deactivateTag(@PathVariable Long id) {
        Tag tag = tagRepo.findById(id).orElse(null);

        if (tag != null) {
            tag.setStatus("INACTIVE");
            tag.setLastUpdatedAt(LocalDateTime.now());
            return tagRepo.save(tag);
        }

        return null;
    }

    @PutMapping("/tag/{id}/activate")
    public Tag activateTag(@PathVariable Long id) {
        Tag tag = tagRepo.findById(id).orElse(null);

        if (tag != null) {
            tag.setStatus("ACTIVE");
            tag.setLastUpdatedAt(LocalDateTime.now());
            return tagRepo.save(tag);
        }

        return null;
    }
}