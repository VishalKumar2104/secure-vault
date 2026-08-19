package com.securevault.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void storeFile(MultipartFile file, String subFolder, String storedName);
    Resource getFile(String subFolder, String storedName);
    void deleteFile(String subFolder, String storedName);
    boolean fileExists(String subFolder, String storedName);
}
