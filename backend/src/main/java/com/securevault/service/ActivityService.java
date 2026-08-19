package com.securevault.service;

import com.securevault.dto.ActivityResponse;
import com.securevault.entity.Activity;
import com.securevault.entity.FileEntity;
import com.securevault.entity.User;
import com.securevault.repository.ActivityRepository;
import com.securevault.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logActivity(User user, FileEntity file, String type, String description, String ipAddress, String userAgent) {
        Activity activity = Activity.builder()
                .user(user)
                .file(file)
                .activityType(type)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserActivities(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Activity> activities = activityRepository.findByUser(user, pageable);
        
        return activities.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentUserActivities(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        List<Activity> activities = activityRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        return activities.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .userEmail(activity.getUser().getEmail())
                .userName(activity.getUser().getFullName())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
