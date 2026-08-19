package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.FileResponse;
import com.securevault.entity.FileEntity;
import com.securevault.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        FileResponse response = fileService.uploadFile(
                principal.getName(),
                file,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFiles(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "newest") String sortBy,
            Principal principal
    ) {
        Page<FileResponse> filePage = fileService.getFiles(
                principal.getName(),
                search,
                filter,
                page,
                size,
                sortBy
        );

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", filePage.getContent());
        pageData.put("page", filePage.getNumber());
        pageData.put("size", filePage.getSize());
        pageData.put("totalElements", filePage.getTotalElements());
        pageData.put("totalPages", filePage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", pageData));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileDetails(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        FileResponse response = fileService.getFileDetails(
                principal.getName(),
                id,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("File details retrieved", response));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        Resource resource = fileService.downloadFile(
                principal.getName(),
                id,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );

        FileEntity fileEntity = fileService.getFileEntity(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        fileService.deleteFile(
                principal.getName(),
                id,
                getClientIp(servletRequest),
                getClientUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getRecentFiles(Principal principal) {
        List<FileResponse> recentFiles = fileService.getRecentFiles(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Recent files retrieved", recentFiles));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(Principal principal) {
        long totalFiles = fileService.getFilesCount(principal.getName());
        long storageUsed = fileService.getStorageUsed(principal.getName());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", totalFiles);
        stats.put("storageUsed", storageUsed);
        
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved", stats));
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
