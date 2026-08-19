package com.securevault.dto;

import java.time.LocalDateTime;

public class ActivityResponse {
    private Long id;
    private String userEmail;
    private String userName;
    private String activityType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public ActivityResponse() {}

    public ActivityResponse(Long id, String userEmail, String userName, String activityType,
                            String description, String ipAddress, String userAgent, LocalDateTime createdAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.userName = userName;
        this.activityType = activityType;
        this.description = description;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public static ActivityResponseBuilder builder() {
        return new ActivityResponseBuilder();
    }

    public static class ActivityResponseBuilder {
        private Long id;
        private String userEmail;
        private String userName;
        private String activityType;
        private String description;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;

        public ActivityResponseBuilder id(Long id) { this.id = id; return this; }
        public ActivityResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public ActivityResponseBuilder userName(String userName) { this.userName = userName; return this; }
        public ActivityResponseBuilder activityType(String activityType) { this.activityType = activityType; return this; }
        public ActivityResponseBuilder description(String description) { this.description = description; return this; }
        public ActivityResponseBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public ActivityResponseBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public ActivityResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ActivityResponse build() {
            ActivityResponse r = new ActivityResponse();
            r.id = this.id;
            r.userEmail = this.userEmail;
            r.userName = this.userName;
            r.activityType = this.activityType;
            r.description = this.description;
            r.ipAddress = this.ipAddress;
            r.userAgent = this.userAgent;
            r.createdAt = this.createdAt;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
