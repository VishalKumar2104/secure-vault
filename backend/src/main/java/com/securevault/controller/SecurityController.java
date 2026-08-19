package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.entity.SecurityEvent;
import com.securevault.service.SecurityEventService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityEventService securityEventService;

    public SecurityController(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSecurityStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("authentication", "Protected");
        status.put("fileStorage", "Secure");
        status.put("accessControl", "Enabled");
        status.put("fileEncryption", "Enabled where supported by the configured storage/database infrastructure");
        status.put("activityMonitoring", "Enabled");
        status.put("vaultStatus", "Protected");
        
        return ResponseEntity.ok(ApiResponse.success("Security status retrieved", status));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal
    ) {
        Page<SecurityEvent> events = securityEventService.getUserSecurityEvents(principal.getName(), page, size);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", events.getContent());
        pageData.put("page", events.getNumber());
        pageData.put("size", events.getSize());
        pageData.put("totalElements", events.getTotalElements());
        pageData.put("totalPages", events.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Security events retrieved successfully", pageData));
    }
}
