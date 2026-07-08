package com.badminton_manager.badminton.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String subDirectory);
    void delete(String publicUrl);
}
