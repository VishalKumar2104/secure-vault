package com.securevault.service;

import com.securevault.entity.SecurityEvent;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.repository.SecurityEventRepository;
import com.securevault.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository, UserRepository userRepository) {
        this.securityEventRepository = securityEventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logSecurityEvent(User user, String type, Severity severity, String description) {
        SecurityEvent event = SecurityEvent.builder()
                .user(user)
                .eventType(type)
                .severity(severity)
                .description(description)
                .build();
        securityEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<SecurityEvent> getUserSecurityEvents(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return securityEventRepository.findByUser(user, pageable);
    }
}
