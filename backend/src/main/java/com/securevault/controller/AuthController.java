package com.securevault.controller;

import com.securevault.dto.*;
import com.securevault.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<AuthResponse>> syncFirebaseUser(
            @RequestBody AuthRequest request,
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        String email = (principal != null) ? principal.getName() : request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required for synchronization", "BAD_REQUEST"));
        }

        AuthResponse response = authService.syncUser(
                email,
                request.getPassword(), // used as optional full name or passed via body
                request.getIdToken(),
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );

        return ResponseEntity.ok(ApiResponse.success("User synchronized successfully", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        authService.register(request, getClientIp(servletRequest), getClientUserAgent(servletRequest));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        String email = (principal != null) ? principal.getName() : null;
        authService.logout(email, getClientIp(servletRequest), getClientUserAgent(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        authService.forgotPassword(request.getEmail(), getClientIp(servletRequest), getClientUserAgent(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("If an account exists for this email, password reset instructions have been sent."));
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
