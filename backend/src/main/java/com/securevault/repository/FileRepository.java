package com.securevault.repository;

import com.securevault.entity.FileEntity;
import com.securevault.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    
    Page<FileEntity> findByUserAndStatusAndOriginalFileNameContainingIgnoreCase(User user, String status, String name, Pageable pageable);

    Page<FileEntity> findByUserAndStatusAndFileTypeInAndOriginalFileNameContainingIgnoreCase(User user, String status, Collection<String> fileTypes, String name, Pageable pageable);

    long countByUserAndStatus(User user, String status);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntity f WHERE f.user = :user AND f.status = 'ACTIVE'")
    Long sumFileSizeByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntity f WHERE f.status = 'ACTIVE'")
    Long sumTotalFileSize();

    List<FileEntity> findTop5ByUserAndStatusOrderByUploadDateDesc(User user, String status);

    // Admin query methods
    Page<FileEntity> findByStatusAndOriginalFileNameContainingIgnoreCase(String status, String name, Pageable pageable);

    Page<FileEntity> findByStatusAndFileTypeInAndOriginalFileNameContainingIgnoreCase(String status, Collection<String> fileTypes, String name, Pageable pageable);

    long countByStatus(String status);
}
