package com.securevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "encryption_status")
    private String encryptionStatus;

    @Column(nullable = false)
    private String status; // ACTIVE, DELETED, etc.

    public FileEntity() {}

    public FileEntity(Long id, User user, String originalFileName, String storedFileName,
                      String filePath, String fileType, String mimeType, Long fileSize,
                      LocalDateTime uploadDate, LocalDateTime updatedAt, String encryptionStatus, String status) {
        this.id = id;
        this.user = user;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.updatedAt = updatedAt;
        this.encryptionStatus = encryptionStatus;
        this.status = status;
    }

    public static FileEntityBuilder builder() {
        return new FileEntityBuilder();
    }

    public static class FileEntityBuilder {
        private Long id;
        private User user;
        private String originalFileName;
        private String storedFileName;
        private String filePath;
        private String fileType;
        private String mimeType;
        private Long fileSize;
        private LocalDateTime uploadDate;
        private LocalDateTime updatedAt;
        private String encryptionStatus;
        private String status;

        public FileEntityBuilder id(Long id) { this.id = id; return this; }
        public FileEntityBuilder user(User user) { this.user = user; return this; }
        public FileEntityBuilder originalFileName(String originalFileName) { this.originalFileName = originalFileName; return this; }
        public FileEntityBuilder storedFileName(String storedFileName) { this.storedFileName = storedFileName; return this; }
        public FileEntityBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public FileEntityBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public FileEntityBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public FileEntityBuilder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public FileEntityBuilder uploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; return this; }
        public FileEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public FileEntityBuilder encryptionStatus(String encryptionStatus) { this.encryptionStatus = encryptionStatus; return this; }
        public FileEntityBuilder status(String status) { this.status = status; return this; }

        public FileEntity build() {
            FileEntity f = new FileEntity();
            f.id = this.id;
            f.user = this.user;
            f.originalFileName = this.originalFileName;
            f.storedFileName = this.storedFileName;
            f.filePath = this.filePath;
            f.fileType = this.fileType;
            f.mimeType = this.mimeType;
            f.fileSize = this.fileSize;
            f.uploadDate = this.uploadDate;
            f.updatedAt = this.updatedAt;
            f.encryptionStatus = this.encryptionStatus;
            f.status = this.status;
            return f;
        }
    }

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEncryptionStatus() { return encryptionStatus; }
    public void setEncryptionStatus(String encryptionStatus) { this.encryptionStatus = encryptionStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
