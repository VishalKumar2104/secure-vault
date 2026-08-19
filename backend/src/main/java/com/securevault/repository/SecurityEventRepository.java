package com.securevault.repository;

import com.securevault.entity.SecurityEvent;
import com.securevault.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    Page<SecurityEvent> findByUser(User user, Pageable pageable);
    long countByEventType(String eventType);
    List<SecurityEvent> findTop10ByOrderByCreatedAtDesc();
}
