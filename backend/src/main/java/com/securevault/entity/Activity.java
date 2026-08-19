package com.securevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Activity() {}

    public Activity(Long id, User user, FileEntity file, String activityType,
                    String description, String ipAddress, String userAgent, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.file = file;
        this.activityType = activityType;
        this.description = description;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public static ActivityBuilder builder() {
        return new ActivityBuilder();
    }

    public static class ActivityBuilder {
        private Long id;
        private User user;
        private FileEntity file;
        private String activityType;
        private String description;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;

        public ActivityBuilder id(Long id) { this.id = id; return this; }
        public ActivityBuilder user(User user) { this.user = user; return this; }
        public ActivityBuilder file(FileEntity file) { this.file = file; return this; }
        public ActivityBuilder activityType(String activityType) { this.activityType = activityType; return this; }
        public ActivityBuilder description(String description) { this.description = description; return this; }
        public ActivityBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public ActivityBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public ActivityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Activity build() {
            Activity a = new Activity();
            a.id = this.id;
            a.user = this.user;
            a.file = this.file;
            a.activityType = this.activityType;
            a.description = this.description;
            a.ipAddress = this.ipAddress;
            a.userAgent = this.userAgent;
            a.createdAt = this.createdAt;
            return a;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public FileEntity getFile() { return file; }
    public void setFile(FileEntity file) { this.file = file; }

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
