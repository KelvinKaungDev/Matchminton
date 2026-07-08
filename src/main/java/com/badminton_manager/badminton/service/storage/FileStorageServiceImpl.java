package com.badminton_manager.badminton.service.storage;

import com.badminton_manager.badminton.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadRoot;
    private final String publicUrlPrefix;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.public-url-prefix:/uploads}") String publicUrlPrefix
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicUrlPrefix = publicUrlPrefix;
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        String filename = UUID.randomUUID() + "." + extensionFor(file.getContentType());

        Path targetDir = uploadRoot.resolve(subDirectory).normalize();
        if (!targetDir.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid upload path");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + filename, e);
        }

        return publicUrlPrefix + "/" + subDirectory + "/" + filename;
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith(publicUrlPrefix + "/")) {
            return;
        }

        String relativePath = publicUrl.substring(publicUrlPrefix.length() + 1);
        Path target = uploadRoot.resolve(relativePath).normalize();
        if (!target.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + target, e);
        }
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            throw new BadRequestException("Missing file content type");
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new BadRequestException(
                    "Unsupported file type: " + contentType + ". Allowed: image/jpeg, image/png, image/webp, image/gif");
        };
    }
}
