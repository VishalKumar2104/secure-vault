package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.ChangePasswordRequest;
import com.securevault.dto.UserResponse;
import com.securevault.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Principal principal) {
        UserResponse response = userService.getProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile details retrieved", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Principal principal,
            @Valid @RequestBody UserResponse request,
            HttpServletRequest servletRequest
    ) {
        UserResponse response = userService.updateProfile(
                principal.getName(),
                request,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        userService.changePassword(
                principal.getName(),
                request,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getClientUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
