package com.securevault.service;

import com.securevault.dto.AuthResponse;
import com.securevault.dto.RegisterRequest;
import com.securevault.entity.AccountStatus;
import com.securevault.entity.Role;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.exception.AccessDeniedException;
import com.securevault.exception.InvalidFileException;
import com.securevault.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final SecurityEventService securityEventService;

    public AuthService(UserRepository userRepository, ActivityService activityService,
                       SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public AuthResponse syncUser(String email, String fullName, String firebaseUid, String ip, String userAgent) {
        final String userEmail = email.toLowerCase().trim();
        User user = userRepository.findByEmail(userEmail).orElse(null);

        if (user == null) {
            Role role = userEmail.equalsIgnoreCase("admin@securevault.com") ? Role.ADMIN : Role.USER;
            String name = (fullName != null && !fullName.isBlank()) ? fullName : userEmail.split("@")[0];

            user = User.builder()
                    .fullName(name)
                    .email(userEmail)
                    .firebaseUid(firebaseUid)
                    .role(role)
                    .accountStatus(AccountStatus.ACTIVE)
                    .lastLogin(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);
            activityService.logActivity(user, null, "REGISTER", "Account registered via Firebase", ip, userAgent);
        } else {
            if (user.getAccountStatus() == AccountStatus.DISABLED) {
                securityEventService.logSecurityEvent(
                        user,
                        "ACCOUNT_DISABLED_ACCESS",
                        Severity.CRITICAL,
                        "Attempted login to a disabled account: " + userEmail + " from IP: " + ip
                );
                throw new AccessDeniedException("Your account is disabled. Please contact administration.");
            }

            if (fullName != null && !fullName.isBlank() && !fullName.equals(user.getFullName())) {
                user.setFullName(fullName);
            }
            if (firebaseUid != null && !firebaseUid.isBlank()) {
                user.setFirebaseUid(firebaseUid);
            }
            user.setLastLogin(LocalDateTime.now());
            user = userRepository.save(user);
        }

        activityService.logActivity(user, null, "LOGIN", "Successfully logged in via Firebase", ip, userAgent);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .firebaseUid(user.getFirebaseUid())
                .build();
    }

    @Transactional
    public void register(RegisterRequest request, String ip, String userAgent) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new InvalidFileException("Email is already registered in the system.");
        }

        Role role = email.equalsIgnoreCase("admin@securevault.com") ? Role.ADMIN : Role.USER;

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .firebaseUid(request.getFirebaseToken())
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        activityService.logActivity(saved, null, "REGISTER", "Account registered successfully", ip, userAgent);
    }

    @Transactional
    public void logout(String email, String ip, String userAgent) {
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
            if (user != null) {
                activityService.logActivity(user, null, "LOGOUT", "User signed out", ip, userAgent);
            }
        }
    }

    @Transactional
    public void forgotPassword(String email, String ip, String userAgent) {
        User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
        if (user != null) {
            activityService.logActivity(user, null, "FORGOT_PASSWORD_REQUEST", "Password reset request initiated", ip, userAgent);
        } else {
            securityEventService.logSecurityEvent(
                    null,
                    "UNTRACKED_FORGOT_PASSWORD",
                    Severity.LOW,
                    "Forgot password requested for non-existing email: " + email + " from IP: " + ip
            );
        }
    }
}
