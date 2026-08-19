package com.securevault.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String accountStatus;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long filesUploaded;
    private Long storageUsed;

    public UserResponse() {}

    public UserResponse(Long id, String fullName, String email, String role, String accountStatus,
                        LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt,
                        Long filesUploaded, Long storageUsed) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.filesUploaded = filesUploaded;
        this.storageUsed = storageUsed;
    }

    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    public static class UserResponseBuilder {
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String accountStatus;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long filesUploaded;
        private Long storageUsed;

        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder role(String role) { this.role = role; return this; }
        public UserResponseBuilder accountStatus(String accountStatus) { this.accountStatus = accountStatus; return this; }
        public UserResponseBuilder lastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; return this; }
        public UserResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public UserResponseBuilder filesUploaded(Long filesUploaded) { this.filesUploaded = filesUploaded; return this; }
        public UserResponseBuilder storageUsed(Long storageUsed) { this.storageUsed = storageUsed; return this; }

        public UserResponse build() {
            UserResponse r = new UserResponse();
            r.id = this.id;
            r.fullName = this.fullName;
            r.email = this.email;
            r.role = this.role;
            r.accountStatus = this.accountStatus;
            r.lastLogin = this.lastLogin;
            r.createdAt = this.createdAt;
            r.updatedAt = this.updatedAt;
            r.filesUploaded = this.filesUploaded;
            r.storageUsed = this.storageUsed;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getFilesUploaded() { return filesUploaded; }
    public void setFilesUploaded(Long filesUploaded) { this.filesUploaded = filesUploaded; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }
}
