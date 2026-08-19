package com.securevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SecurityEvent() {}

    public SecurityEvent(Long id, User user, String eventType, Severity severity,
                         String description, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static SecurityEventBuilder builder() {
        return new SecurityEventBuilder();
    }

    public static class SecurityEventBuilder {
        private Long id;
        private User user;
        private String eventType;
        private Severity severity;
        private String description;
        private LocalDateTime createdAt;

        public SecurityEventBuilder id(Long id) { this.id = id; return this; }
        public SecurityEventBuilder user(User user) { this.user = user; return this; }
        public SecurityEventBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public SecurityEventBuilder severity(Severity severity) { this.severity = severity; return this; }
        public SecurityEventBuilder description(String description) { this.description = description; return this; }
        public SecurityEventBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SecurityEvent build() {
            SecurityEvent s = new SecurityEvent();
            s.id = this.id;
            s.user = this.user;
            s.eventType = this.eventType;
            s.severity = this.severity;
            s.description = this.description;
            s.createdAt = this.createdAt;
            return s;
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

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
