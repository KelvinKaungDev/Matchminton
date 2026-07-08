package com.badminton_manager.badminton.service.storage;

import com.badminton_manager.badminton.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl(tempDir.toString(), "/uploads");
    }

    @Test
    void store_validImage_savesFileAndReturnsPublicUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "fake-image-bytes".getBytes());

        String url = fileStorageService.store(file, "groups");

        assertThat(url).startsWith("/uploads/groups/").endsWith(".png");

        Path stored = tempDir.resolve("groups").resolve(url.substring("/uploads/groups/".length()));
        assertThat(Files.exists(stored)).isTrue();
    }

    @Test
    void store_emptyFile_throwsBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.store(emptyFile, "groups"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void store_unsupportedContentType_throwsBadRequestException() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(pdfFile, "groups"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void delete_existingFile_removesItFromDisk() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        String url = fileStorageService.store(file, "groups");
        Path stored = tempDir.resolve("groups").resolve(url.substring("/uploads/groups/".length()));
        assertThat(Files.exists(stored)).isTrue();

        fileStorageService.delete(url);

        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void delete_foreignUrl_doesNothing() {
        fileStorageService.delete("https://example.com/some-other-photo.png");
        fileStorageService.delete(null);
    }
}
