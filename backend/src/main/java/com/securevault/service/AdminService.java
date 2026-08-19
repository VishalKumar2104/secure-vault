package com.securevault.service;

import com.securevault.dto.ActivityResponse;
import com.securevault.dto.FileResponse;
import com.securevault.dto.UserResponse;
import com.securevault.entity.*;
import com.securevault.repository.ActivityRepository;
import com.securevault.repository.FileRepository;
import com.securevault.repository.SecurityEventRepository;
import com.securevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final ActivityRepository activityRepository;
    private final SecurityEventRepository securityEventRepository;
    private final ActivityService activityService;

    @Value("${file.storage.location:./storage}")
    private String storageLocation;

    public AdminService(UserRepository userRepository, FileRepository fileRepository,
                        ActivityRepository activityRepository, SecurityEventRepository securityEventRepository,
                        ActivityService activityService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.activityRepository = activityRepository;
        this.securityEventRepository = securityEventRepository;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long totalFiles = fileRepository.countByStatus("ACTIVE");
        long storageConsumed = fileRepository.sumTotalFileSize();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus() == AccountStatus.ACTIVE)
                .count();
        long failedLoginAttempts = securityEventRepository.countByEventType("FAILED_LOGIN");
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalFiles", totalFiles);
        stats.put("storageConsumed", storageConsumed);
        stats.put("activeUsers", activeUsers);
        stats.put("failedLoginAttempts", failedLoginAttempts);
        stats.put("systemSecurity", "Protected");
        
        return stats;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(user -> {
            long filesCount = fileRepository.countByUserAndStatus(user, "ACTIVE");
            Long storageUsed = fileRepository.sumFileSizeByUser(user);
            
            return UserResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .accountStatus(user.getAccountStatus().name())
                    .lastLogin(user.getLastLogin())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .filesUploaded(filesCount)
                    .storageUsed(storageUsed != null ? storageUsed : 0L)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> getAllFiles(String search, String filterType, int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "uploadDate");
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "oldest":
                    sort = Sort.by(Sort.Direction.ASC, "uploadDate");
                    break;
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "originalFileName");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "originalFileName");
                    break;
                case "size_desc":
                    sort = Sort.by(Sort.Direction.DESC, "fileSize");
                    break;
                case "size_asc":
                    sort = Sort.by(Sort.Direction.ASC, "fileSize");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        String nameQuery = search != null ? search.trim() : "";
        List<String> extensions = mapFilterTypeToExtensions(filterType);

        Page<FileEntity> files;
        if (extensions != null) {
            files = fileRepository.findByStatusAndFileTypeInAndOriginalFileNameContainingIgnoreCase(
                    "ACTIVE", extensions, nameQuery, pageable
            );
        } else {
            files = fileRepository.findByStatusAndOriginalFileNameContainingIgnoreCase(
                    "ACTIVE", nameQuery, pageable
            );
        }

        return files.map(this::mapToFileResponse);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getAllActivities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Activity> activities = activityRepository.findAll(pageable);
        return activities.map(activityService::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<SecurityEvent> getAllSecurityEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return securityEventRepository.findAll(pageable);
    }

    @Transactional
    public void toggleUserAccountStatus(Long userId, String emailWhoPerformed) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Prevent admin disabling themselves
        if (targetUser.getEmail().equalsIgnoreCase(emailWhoPerformed)) {
            throw new IllegalStateException("You cannot disable your own admin account.");
        }

        if (targetUser.getAccountStatus() == AccountStatus.ACTIVE) {
            targetUser.setAccountStatus(AccountStatus.DISABLED);
        } else {
            targetUser.setAccountStatus(AccountStatus.ACTIVE);
        }
        userRepository.save(targetUser);
    }

    public Map<String, String> getInfrastructureHealth() {
        Map<String, String> health = new HashMap<>();

        // 1. Application Server Check
        health.put("server", "Operational");

        // 2. REST API Check
        health.put("api", "Operational");

        // 3. Database Check
        try {
            userRepository.count();
            health.put("database", "Operational");
        } catch (Exception e) {
            health.put("database", "Unavailable");
        }

        // 4. File Storage Check
        try {
            var path = Paths.get(storageLocation);
            if (Files.exists(path)) {
                if (Files.isWritable(path)) {
                    health.put("storage", "Operational");
                } else {
                    health.put("storage", "Warning");
                }
            } else {
                Files.createDirectories(path);
                health.put("storage", "Operational");
            }
        } catch (Exception e) {
            health.put("storage", "Unavailable");
        }

        return health;
    }

    private FileResponse mapToFileResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .fileType(file.getFileType())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .uploadDate(file.getUploadDate())
                .updatedAt(file.getUpdatedAt())
                .encryptionStatus(file.getEncryptionStatus())
                .status(file.getStatus())
                .ownerEmail(file.getUser().getEmail())
                .ownerName(file.getUser().getFullName())
                .build();
    }

    private List<String> mapFilterTypeToExtensions(String filterType) {
        if (filterType == null || filterType.isBlank() || filterType.equalsIgnoreCase("all")) {
            return null;
        }

        switch (filterType.toLowerCase()) {
            case "pdf":
                return Collections.singletonList("pdf");
            case "images":
                return Arrays.asList("png", "jpg", "jpeg");
            case "documents":
                return Arrays.asList("doc", "docx", "txt");
            case "spreadsheets":
                return Arrays.asList("xls", "xlsx");
            case "other":
                return Collections.singletonList("zip");
            default:
                return null;
        }
    }
}
