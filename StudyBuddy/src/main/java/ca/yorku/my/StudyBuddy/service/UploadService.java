package ca.yorku.my.StudyBuddy;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Service
/**
 * This class handles upload and retrieval of chat attachment files on local storage.
 */
public class UploadService {

    private static final Path BASE_UPLOAD_DIR = Paths.get("uploads", "chat-attachments");

    /**
     * Stores a user attachment under uploads/chat-attachments/<actorId>/ and
     * returns metadata used by chat message payloads.
     */
    public FileAttachmentDTO uploadAttachment(MultipartFile file, String actorId) {
        // Basic request validation before touching filesystem.
        if (isBlank(actorId)) {
            throw new ValidationException("actorId is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ValidationException("file is required");
        }

        String originalName = file.getOriginalFilename();
        if (isBlank(originalName)) {
            throw new ValidationException("file name is required");
        }

        String safeActorId = sanitizePathSegment(actorId);
        String safeName = sanitizeFileName(originalName);
        String storedName = Instant.now().toEpochMilli() + "_" + safeName;
        Path actorDir = BASE_UPLOAD_DIR.resolve(safeActorId).normalize();
        Path targetFile = actorDir.resolve(storedName).normalize();

        // Prevent path traversal: final path must remain inside actor directory.
        if (!targetFile.startsWith(actorDir)) {
            throw new ValidationException("invalid file path");
        }

        try {
            Files.createDirectories(actorDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to upload file", exception);
        }

        FileAttachmentDTO response = new FileAttachmentDTO();
        response.setFileName(safeName);
        response.setFileSizeBytes(file.getSize());
        response.setMimeType(isBlank(file.getContentType()) ? "application/octet-stream" : file.getContentType());
        response.setStoragePath("/api/uploads/files/" + safeActorId + "/" + storedName);
        return response;
    }

    /**
     * Resolves and loads a previously uploaded file and returns resource data
     * required by the download endpoint.
     */
    public DownloadedFile loadAttachment(String actorId, String storedFileName, String requestedDownloadName) {
        if (isBlank(actorId) || isBlank(storedFileName)) {
            throw new ValidationException("actorId and fileName are required");
        }

        String safeActorId = sanitizePathSegment(actorId);
        String safeStoredName = sanitizeFileName(storedFileName);
        String downloadName = isBlank(requestedDownloadName) ? safeStoredName : sanitizeFileName(requestedDownloadName);

        Path actorDir = BASE_UPLOAD_DIR.resolve(safeActorId).normalize();
        Path targetFile = actorDir.resolve(safeStoredName).normalize();

        // Prevent path traversal by enforcing actor directory boundary.
        if (!targetFile.startsWith(actorDir)) {
            throw new ValidationException("invalid file path");
        }
        if (!Files.exists(targetFile) || !Files.isRegularFile(targetFile)) {
            throw new NotFoundException("File not found");
        }

        String contentType;
        try {
            contentType = Files.probeContentType(targetFile);
        } catch (IOException exception) {
            contentType = null;
        }

        Resource resource = new PathResource(targetFile);
        return new DownloadedFile(resource, isBlank(contentType) ? "application/octet-stream" : contentType, downloadName);
    }

    /**
     * Sanitizes user-provided file names to a safe subset for storage.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Sanitizes path segments (such as actor ids) to safe filesystem tokens.
     */
    private String sanitizePathSegment(String segment) {
        return segment.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Shared utility for null/blank string checks.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Compact value object used by controller to build download responses.
     */
    public record DownloadedFile(Resource resource, String contentType, String downloadName) {
    }
}
