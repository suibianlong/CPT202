package com.cpt202.group8.heritage.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachedFile")
public class AttachedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    @ManyToOne
    @JoinColumn(name = "feedbackId", nullable = false)
    private Feedback feedback;

    @Column(name = "originalFilename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "storedFilename", nullable = false, length = 50)
    private String storedFilename;

    @Column(name = "filePath", nullable = false, length = 500)
    private String filePath;

    @Column(name = "fileType", nullable = false, length = 20)
    private String fileType;

    @Column(name = "fileSize", nullable = false)
    private Long fileSize;

    @Column(name = "uploadedAt", nullable = false)
    private LocalDateTime uploadedAt;

    public AttachedFile() {
    }

    public AttachedFile(Feedback feedback, String originalFilename, String storedFilename,
                        String filePath, String fileType, Long fileSize, LocalDateTime uploadedAt) {
        this.feedback = feedback;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}