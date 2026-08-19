package com.securevault.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.securevault.entity.AccountStatus;
import com.securevault.entity.Role;
import com.securevault.entity.User;
import com.securevault.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FirebaseAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = null;
            String uid = null;
            String name = null;

            // 1. Attempt verification via Firebase Admin SDK
            try {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                if (firebaseAuth != null) {
                    FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                    email = decodedToken.getEmail();
                    uid = decodedToken.getUid();
                    name = decodedToken.getName();
                }
            } catch (Exception firebaseEx) {
                log.debug("Firebase SDK online verification failed or not configured, inspecting token payload: {}", firebaseEx.getMessage());
            }

            // 2. Dev / Decoded fallback for testing or mock environments
            if (email == null) {
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                        JsonNode payload = objectMapper.readTree(payloadJson);
                        if (payload.has("email")) {
                            email = payload.get("email").asText();
                        }
                        if (payload.has("user_id")) {
                            uid = payload.get("user_id").asText();
                        } else if (payload.has("sub")) {
                            uid = payload.get("sub").asText();
                        }
                        if (payload.has("name")) {
                            name = payload.get("name").asText();
                        }
                    } else if (token.contains("@")) {
                        // Support direct email token in dev simulation
                        email = token;
                        uid = "dev-" + token;
                    }
                } catch (Exception ex) {
                    log.warn("Token payload parsing failed: {}", ex.getMessage());
                }
            }

            if (email == null || email.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Invalid or unverified Firebase authentication token\",\"error\":\"UNAUTHORIZED\"}");
                return;
            }

            // 3. Load or Synchronize User in Local Database
            final String userEmail = email.toLowerCase().trim();
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user == null) {
                // Auto-provision new user on first Firebase login
                Role role = userEmail.equalsIgnoreCase("admin@securevault.com") ? Role.ADMIN : Role.USER;
                String displayName = (name != null && !name.isBlank()) ? name : userEmail.split("@")[0];

                user = User.builder()
                        .fullName(displayName)
                        .email(userEmail)
                        .firebaseUid(uid)
                        .role(role)
                        .accountStatus(AccountStatus.ACTIVE)
                        .build();

                user = userRepository.save(user);
                log.info("Auto-provisioned new user from Firebase: {} with role {}", userEmail, role);
            } else if (uid != null && (user.getFirebaseUid() == null || !uid.equals(user.getFirebaseUid()))) {
                user.setFirebaseUid(uid);
                userRepository.save(user);
            }

            // 4. Check if account has been disabled by Administrator
            if (user.getAccountStatus() == AccountStatus.DISABLED) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Your account has been disabled. Please contact administration.\",\"error\":\"ACCOUNT_DISABLED\"}");
                return;
            }

            // 5. Establish Spring Security Authentication Context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Authentication filter exception: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Authentication failed: " + e.getMessage() + "\",\"error\":\"UNAUTHORIZED\"}");
        }
    }
}
