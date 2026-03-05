package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.FileAttachmentDTO;
import ca.yorku.my.StudyBuddy.NotFoundException;
import ca.yorku.my.StudyBuddy.UploadService;
import ca.yorku.my.StudyBuddy.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for upload business rules including validation, persistence, and retrieval.
 */
class UploadServiceUnitTests {

    private final UploadService uploadService = new UploadService();
    private static final Path BASE_UPLOAD_DIR = Path.of("uploads", "chat-attachments");

    @AfterEach
    void cleanupUploads() throws IOException {
        if (Files.exists(BASE_UPLOAD_DIR)) {
            Files.walk(BASE_UPLOAD_DIR)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    /**
     * Verifies upload stores bytes on disk and returns expected metadata fields.
     */
    @Test
    void uploadAttachmentStoresFileAndReturnsMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                "hello-world".getBytes()
        );

        FileAttachmentDTO response = uploadService.uploadAttachment(file, "u1");

        assertNotNull(response.getStoragePath());
        assertTrue(response.getStoragePath().startsWith("/api/uploads/files/u1/"));
        assertEquals("notes.pdf", response.getFileName());
        assertEquals(file.getSize(), response.getFileSizeBytes());

        String storedFileName = response.getStoragePath().substring(response.getStoragePath().lastIndexOf('/') + 1);
        Path storedPath = BASE_UPLOAD_DIR.resolve("u1").resolve(storedFileName);
        assertTrue(Files.exists(storedPath));
    }

    /**
     * Verifies loading an existing file returns resource data and requested download name.
     */
    @Test
    void loadAttachmentReturnsDownloadedFileForExistingPath() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sheet.txt",
                "text/plain",
                "content".getBytes()
        );

        FileAttachmentDTO uploaded = uploadService.uploadAttachment(file, "u2");
        String storedFileName = uploaded.getStoragePath().substring(uploaded.getStoragePath().lastIndexOf('/') + 1);

        UploadService.DownloadedFile downloaded = uploadService.loadAttachment("u2", storedFileName, "custom-name.txt");

        assertEquals("custom-name.txt", downloaded.downloadName());
        assertNotNull(downloaded.resource());
        assertTrue(downloaded.resource().exists());
    }

    /**
     * Verifies actor id validation rejects blank identities on upload.
     */
    @Test
    void uploadAttachmentRejectsBlankActorId() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());

        assertThrows(ValidationException.class, () -> uploadService.uploadAttachment(file, " "));
    }

    /**
     * Verifies missing files are surfaced as NotFoundException.
     */
    @Test
    void loadAttachmentThrowsNotFoundForMissingFile() {
        assertThrows(NotFoundException.class,
                () -> uploadService.loadAttachment("u3", "missing.txt", null));
    }
}
