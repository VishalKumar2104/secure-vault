package com.securevault.service;

import com.securevault.dto.FileResponse;
import com.securevault.entity.FileEntity;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.exception.AccessDeniedException;
import com.securevault.exception.InvalidFileException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.repository.FileRepository;
import com.securevault.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class FileService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx", "txt", "zip"
    );

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ActivityService activityService;
    private final SecurityEventService securityEventService;

    public FileService(FileRepository fileRepository, UserRepository userRepository,
                       StorageService storageService, ActivityService activityService,
                       SecurityEventService securityEventService) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.activityService = activityService;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public FileResponse uploadFile(String email, MultipartFile file, String ip, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file");
        }

        // 50MB file size limit check (50 * 1024 * 1024)
        if (file.getSize() > 52428800) {
            throw new InvalidFileException("File size exceeds maximum limit of 50MB");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFileName.contains("..")) {
            throw new InvalidFileException("Filename contains invalid path sequence: " + originalFileName);
        }

        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            securityEventService.logSecurityEvent(
                    user,
                    "SUSPICIOUS_UPLOAD_BLOCKED",
                    Severity.HIGH,
                    "Attempted to upload blocked file extension: " + extension + " (" + originalFileName + ") from IP: " + ip
            );
            throw new InvalidFileException("Unsupported file type. Only PDF, images, documents, spreadsheets, and ZIP are allowed.");
        }

        String storedName = UUID.randomUUID().toString() + "." + extension;
        String subFolder = "user-" + user.getId();
        String filePath = subFolder + "/" + storedName;

        // Store physical file
        storageService.storeFile(file, subFolder, storedName);

        FileEntity fileEntity = FileEntity.builder()
                .user(user)
                .originalFileName(originalFileName)
                .storedFileName(storedName)
                .filePath(filePath)
                .fileType(extension)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .encryptionStatus("Protected through authenticated access and controlled server-side storage")
                .status("ACTIVE")
                .build();

        FileEntity saved = fileRepository.save(fileEntity);

        activityService.logActivity(user, saved, "FILE_UPLOAD", "Uploaded file: " + originalFileName, ip, userAgent);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> getFiles(String email, String search, String filterType, int page, int size, String sortBy) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // Setup Sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "uploadDate"); // default
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "oldest":
                    sort = Sort.by(Sort.Direction.ASC, "uploadDate");
                    break;
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "originalFileName");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "originalFileName");
                    break;
                case "size_desc":
                    sort = Sort.by(Sort.Direction.DESC, "fileSize");
                    break;
                case "size_asc":
                    sort = Sort.by(Sort.Direction.ASC, "fileSize");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        String nameQuery = search != null ? search.trim() : "";

        Page<FileEntity> files;
        List<String> extensions = mapFilterTypeToExtensions(filterType);

        if (extensions != null) {
            files = fileRepository.findByUserAndStatusAndFileTypeInAndOriginalFileNameContainingIgnoreCase(
                    user, "ACTIVE", extensions, nameQuery, pageable
            );
        } else {
            files = fileRepository.findByUserAndStatusAndOriginalFileNameContainingIgnoreCase(
                    user, "ACTIVE", nameQuery, pageable
            );
        }

        return files.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public FileResponse getFileDetails(String email, Long id, String ip, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));

        // Enforce STRICT User Ownership Check
        if (!file.getUser().getId().equals(user.getId()) && user.getRole() != com.securevault.entity.Role.ADMIN) {
            securityEventService.logSecurityEvent(
                    user,
                    "UNAUTHORIZED_FILE_ACCESS",
                    Severity.CRITICAL,
                    "User: " + email + " tried to view metadata of file ID: " + id + " belonging to user ID: " + file.getUser().getId() + " from IP: " + ip
            );
            throw new AccessDeniedException("Access Denied: You do not own this file.");
        }

        return mapToResponse(file);
    }

    @Transactional
    public Resource downloadFile(String email, Long id, String ip, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));

        // Enforce STRICT User Ownership Check
        if (!file.getUser().getId().equals(user.getId()) && user.getRole() != com.securevault.entity.Role.ADMIN) {
            securityEventService.logSecurityEvent(
                    user,
                    "UNAUTHORIZED_FILE_DOWNLOAD",
                    Severity.CRITICAL,
                    "User: " + email + " tried to download file ID: " + id + " belonging to user ID: " + file.getUser().getId() + " from IP: " + ip
            );
            throw new AccessDeniedException("Access Denied: You do not have permission to download this file.");
        }

        String subFolder = "user-" + file.getUser().getId();
        Resource resource = storageService.getFile(subFolder, file.getStoredFileName());

        activityService.logActivity(user, file, "FILE_DOWNLOAD", "Downloaded file: " + file.getOriginalFileName(), ip, userAgent);

        return resource;
    }

    @Transactional(readOnly = true)
    public FileEntity getFileEntity(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));
    }

    @Transactional
    public void deleteFile(String email, Long id, String ip, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));

        // Enforce STRICT User Ownership Check
        if (!file.getUser().getId().equals(user.getId()) && user.getRole() != com.securevault.entity.Role.ADMIN) {
            securityEventService.logSecurityEvent(
                    user,
                    "UNAUTHORIZED_FILE_DELETE",
                    Severity.CRITICAL,
                    "User: " + email + " tried to delete file ID: " + id + " belonging to user ID: " + file.getUser().getId() + " from IP: " + ip
            );
            throw new AccessDeniedException("Access Denied: You do not have permission to delete this file.");
        }

        // Delete physical file
        String subFolder = "user-" + file.getUser().getId();
        storageService.deleteFile(subFolder, file.getStoredFileName());

        // Delete metadata
        fileRepository.delete(file);

        activityService.logActivity(user, null, "FILE_DELETE", "Deleted file: " + file.getOriginalFileName(), ip, userAgent);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getRecentFiles(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        
        List<FileEntity> files = fileRepository.findTop5ByUserAndStatusOrderByUploadDateDesc(user, "ACTIVE");
        
        List<FileResponse> list = new ArrayList<>();
        for (FileEntity f : files) {
            list.add(mapToResponse(f));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public long getFilesCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return fileRepository.countByUserAndStatus(user, "ACTIVE");
    }

    @Transactional(readOnly = true)
    public long getStorageUsed(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return fileRepository.sumFileSizeByUser(user);
    }

    private FileResponse mapToResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .fileType(file.getFileType())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .uploadDate(file.getUploadDate())
                .updatedAt(file.getUpdatedAt())
                .encryptionStatus(file.getEncryptionStatus())
                .status(file.getStatus())
                .ownerEmail(file.getUser().getEmail())
                .ownerName(file.getUser().getFullName())
                .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private List<String> mapFilterTypeToExtensions(String filterType) {
        if (filterType == null || filterType.isBlank() || filterType.equalsIgnoreCase("all")) {
            return null;
        }

        switch (filterType.toLowerCase()) {
            case "pdf":
                return Collections.singletonList("pdf");
            case "images":
                return Arrays.asList("png", "jpg", "jpeg");
            case "documents":
                return Arrays.asList("doc", "docx", "txt");
            case "spreadsheets":
                return Arrays.asList("xls", "xlsx");
            case "other":
                return Collections.singletonList("zip");
            default:
                return null;
        }
    }
}
