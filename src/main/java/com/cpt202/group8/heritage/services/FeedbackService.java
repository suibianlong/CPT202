package com.cpt202.group8.heritage.services;

import com.cpt202.group8.heritage.entities.AttachedFile;
import com.cpt202.group8.heritage.entities.Feedback;
import com.cpt202.group8.heritage.repositories.AttachedFileRepository;
import com.cpt202.group8.heritage.repositories.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AttachedFileRepository attachedFileRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 3;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/feedback";

    public FeedbackService(FeedbackRepository feedbackRepository,
                           AttachedFileRepository attachedFileRepository) {
        this.feedbackRepository = feedbackRepository;
        this.attachedFileRepository = attachedFileRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Feedback submitFeedback(Long userId, String feedbackType, String description, MultipartFile[] files) throws IOException {
        if (userId == null) {
            throw new IllegalArgumentException("User is not logged in.");
        }

        if (feedbackType == null || feedbackType.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a feedback type.");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter feedback description.");
        }

        int actualFileCount = countValidFiles(files);
        if (actualFileCount > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("You can upload up to 3 files only.");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(feedbackType.trim());
        feedback.setDescription(description.trim());
        feedback.setFileNum(actualFileCount);
        feedback.setUploadedAt(LocalDateTime.now());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        if (actualFileCount > 0) {
            saveFiles(savedFeedback, files);
        }

        return savedFeedback;
    }

    public List<Feedback> getVisibleFeedbacks(Long userId, String userRole) {
        if ("reviewer".equalsIgnoreCase(userRole)) {
            return feedbackRepository.findAll();
        }
        return feedbackRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    public List<Feedback> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Feedback getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found."));
    }

    private int countValidFiles(MultipartFile[] files) {
        if (files == null) {
            return 0;
        }

        int count = 0;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void saveFiles(Feedback feedback, MultipartFile[] files) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            validateFile(file);

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename).toLowerCase();
            String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            Path filePath = uploadPath.resolve(storedFilename);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());
            file.transferTo(filePath.toFile());

            AttachedFile attachedFile = new AttachedFile();
            attachedFile.setFeedback(feedback);
            attachedFile.setOriginalFilename(originalFilename);
            attachedFile.setStoredFilename(storedFilename);
            attachedFile.setFilePath("/uploads/feedback/" + storedFilename);
            attachedFile.setFileType(extension.toUpperCase());
            attachedFile.setFileSize(file.getSize());
            attachedFile.setUploadedAt(LocalDateTime.now());

            attachedFileRepository.save(attachedFile);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Each file must be 10MB or smaller.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();

        if (!extension.equals("jpg")
                && !extension.equals("png")
                && !extension.equals("pdf")
                && !extension.equals("txt")) {
            throw new IllegalArgumentException("Only JPG, PNG, PDF and TXT files are allowed.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}