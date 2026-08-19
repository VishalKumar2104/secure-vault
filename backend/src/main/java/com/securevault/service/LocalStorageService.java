package com.securevault.service;

import com.securevault.exception.FileUploadException;
import com.securevault.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${file.storage.location:./storage}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileUploadException("Could not initialize storage location", e);
        }
    }

    @Override
    public void storeFile(MultipartFile file, String subFolder, String storedName) {
        try {
            // Defend against path traversal
            if (storedName.contains("..")) {
                throw new FileUploadException("Cannot store file with relative path outside current directory: " + storedName);
            }

            Path userDir = this.rootLocation.resolve(subFolder).normalize();
            Files.createDirectories(userDir);

            Path destinationFile = userDir.resolve(storedName).normalize();
            
            // Ensure destination is still within the root directory
            if (!destinationFile.getParent().startsWith(this.rootLocation)) {
                throw new FileUploadException("Cannot store file outside configured storage directory.");
            }

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file: " + storedName, e);
        }
    }

    @Override
    public Resource getFile(String subFolder, String storedName) {
        try {
            Path userDir = this.rootLocation.resolve(subFolder).normalize();
            Path file = userDir.resolve(storedName).normalize();
            
            if (!file.getParent().startsWith(this.rootLocation)) {
                throw new ResourceNotFoundException("Cannot access file outside configured storage directory.");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file: " + storedName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not read file: " + storedName, e);
        }
    }

    @Override
    public void deleteFile(String subFolder, String storedName) {
        try {
            Path userDir = this.rootLocation.resolve(subFolder).normalize();
            Path file = userDir.resolve(storedName).normalize();
            
            if (!file.getParent().startsWith(this.rootLocation)) {
                throw new FileUploadException("Cannot delete file outside configured storage directory.");
            }

            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new FileUploadException("Could not delete file: " + storedName, e);
        }
    }

    @Override
    public boolean fileExists(String subFolder, String storedName) {
        Path userDir = this.rootLocation.resolve(subFolder).normalize();
        Path file = userDir.resolve(storedName).normalize();
        
        if (!file.getParent().startsWith(this.rootLocation)) {
            return false;
        }
        
        return Files.exists(file);
    }
}
