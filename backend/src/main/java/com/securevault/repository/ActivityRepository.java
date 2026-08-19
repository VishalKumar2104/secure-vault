package com.securevault.repository;

import com.securevault.entity.Activity;
import com.securevault.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByUser(User user, Pageable pageable);
    List<Activity> findTop10ByUserOrderByCreatedAtDesc(User user);
    List<Activity> findTop10ByOrderByCreatedAtDesc();
}
