package com.securevault.controller;

import com.securevault.dto.ActivityResponse;
import com.securevault.dto.ApiResponse;
import com.securevault.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal
    ) {
        Page<ActivityResponse> activities = activityService.getUserActivities(principal.getName(), page, size);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", activities.getContent());
        pageData.put("page", activities.getNumber());
        pageData.put("size", activities.getSize());
        pageData.put("totalElements", activities.getTotalElements());
        pageData.put("totalPages", activities.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Activities retrieved successfully", pageData));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getRecentActivities(Principal principal) {
        List<ActivityResponse> recent = activityService.getRecentUserActivities(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Recent activities retrieved", recent));
    }
}
