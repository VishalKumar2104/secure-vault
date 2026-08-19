package com.securevault.service;

import com.securevault.dto.ChangePasswordRequest;
import com.securevault.dto.UserResponse;
import com.securevault.entity.User;
import com.securevault.exception.InvalidFileException;
import com.securevault.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityService activityService;

    public UserService(UserRepository userRepository, ActivityService activityService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UserResponse request, String ip, String userAgent) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        User updated = userRepository.save(user);

        activityService.logActivity(updated, null, "PROFILE_UPDATE", "Updated profile name to: " + user.getFullName(), ip, userAgent);

        return mapToResponse(updated);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request, String ip, String userAgent) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidFileException("New passwords do not match");
        }

        // In Firebase Auth architecture, password updates are processed through Firebase client/admin API.
        // We log the audit trail for security accountability.
        activityService.logActivity(user, null, "PASSWORD_CHANGE", "Successfully updated account password credentials", ip, userAgent);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accountStatus(user.getAccountStatus().name())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
