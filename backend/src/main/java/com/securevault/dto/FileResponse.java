package com.securevault.dto;

import java.time.LocalDateTime;

public class FileResponse {
    private Long id;
    private String originalFileName;
    private String fileType;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private LocalDateTime updatedAt;
    private String encryptionStatus;
    private String status;
    private String ownerEmail;
    private String ownerName;

    public FileResponse() {}

    public FileResponse(Long id, String originalFileName, String fileType, String mimeType,
                        Long fileSize, LocalDateTime uploadDate, LocalDateTime updatedAt,
                        String encryptionStatus, String status, String ownerEmail, String ownerName) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.updatedAt = updatedAt;
        this.encryptionStatus = encryptionStatus;
        this.status = status;
        this.ownerEmail = ownerEmail;
        this.ownerName = ownerName;
    }

    public static FileResponseBuilder builder() {
        return new FileResponseBuilder();
    }

    public static class FileResponseBuilder {
        private Long id;
        private String originalFileName;
        private String fileType;
        private String mimeType;
        private Long fileSize;
        private LocalDateTime uploadDate;
        private LocalDateTime updatedAt;
        private String encryptionStatus;
        private String status;
        private String ownerEmail;
        private String ownerName;

        public FileResponseBuilder id(Long id) { this.id = id; return this; }
        public FileResponseBuilder originalFileName(String originalFileName) { this.originalFileName = originalFileName; return this; }
        public FileResponseBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public FileResponseBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public FileResponseBuilder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public FileResponseBuilder uploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; return this; }
        public FileResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public FileResponseBuilder encryptionStatus(String encryptionStatus) { this.encryptionStatus = encryptionStatus; return this; }
        public FileResponseBuilder status(String status) { this.status = status; return this; }
        public FileResponseBuilder ownerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; return this; }
        public FileResponseBuilder ownerName(String ownerName) { this.ownerName = ownerName; return this; }

        public FileResponse build() {
            FileResponse r = new FileResponse();
            r.id = this.id;
            r.originalFileName = this.originalFileName;
            r.fileType = this.fileType;
            r.mimeType = this.mimeType;
            r.fileSize = this.fileSize;
            r.uploadDate = this.uploadDate;
            r.updatedAt = this.updatedAt;
            r.encryptionStatus = this.encryptionStatus;
            r.status = this.status;
            r.ownerEmail = this.ownerEmail;
            r.ownerName = this.ownerName;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

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

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
