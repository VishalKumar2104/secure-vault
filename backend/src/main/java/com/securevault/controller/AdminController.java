package com.securevault.controller;

import com.securevault.dto.ActivityResponse;
import com.securevault.dto.ApiResponse;
import com.securevault.dto.FileResponse;
import com.securevault.dto.UserResponse;
import com.securevault.entity.SecurityEvent;
import com.securevault.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<UserResponse> usersPage = adminService.getUsers(page, size);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", usersPage.getContent());
        pageData.put("page", usersPage.getNumber());
        pageData.put("size", usersPage.getSize());
        pageData.put("totalElements", usersPage.getTotalElements());
        pageData.put("totalPages", usersPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Users list retrieved successfully", pageData));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            Principal principal
    ) {
        adminService.toggleUserAccountStatus(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("User account status updated successfully"));
    }

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllFiles(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "newest") String sortBy
    ) {
        Page<FileResponse> filesPage = adminService.getAllFiles(search, filter, page, size, sortBy);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", filesPage.getContent());
        pageData.put("page", filesPage.getNumber());
        pageData.put("size", filesPage.getSize());
        pageData.put("totalElements", filesPage.getTotalElements());
        pageData.put("totalPages", filesPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("All files list retrieved successfully", pageData));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllActivities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<ActivityResponse> activitiesPage = adminService.getAllActivities(page, size);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", activitiesPage.getContent());
        pageData.put("page", activitiesPage.getNumber());
        pageData.put("size", activitiesPage.getSize());
        pageData.put("totalElements", activitiesPage.getTotalElements());
        pageData.put("totalPages", activitiesPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("System activities log retrieved successfully", pageData));
    }

    @GetMapping("/security")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSecurityEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<SecurityEvent> securityPage = adminService.getAllSecurityEvents(page, size);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", securityPage.getContent());
        pageData.put("page", securityPage.getNumber());
        pageData.put("size", securityPage.getSize());
        pageData.put("totalElements", securityPage.getTotalElements());
        pageData.put("totalPages", securityPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("System security events log retrieved successfully", pageData));
    }

    @GetMapping("/infrastructure")
    public ResponseEntity<ApiResponse<Map<String, String>>> getInfrastructureHealth() {
        Map<String, String> health = adminService.getInfrastructureHealth();
        return ResponseEntity.ok(ApiResponse.success("Infrastructure status retrieved successfully", health));
    }
}
