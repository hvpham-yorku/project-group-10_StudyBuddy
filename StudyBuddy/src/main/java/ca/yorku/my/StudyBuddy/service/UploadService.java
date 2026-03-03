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
public class UploadService {

    private static final Path BASE_UPLOAD_DIR = Paths.get("uploads", "chat-attachments");

    public FileAttachmentDTO uploadAttachment(MultipartFile file, String actorId) {
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

    public DownloadedFile loadAttachment(String actorId, String storedFileName, String requestedDownloadName) {
        if (isBlank(actorId) || isBlank(storedFileName)) {
            throw new ValidationException("actorId and fileName are required");
        }

        String safeActorId = sanitizePathSegment(actorId);
        String safeStoredName = sanitizeFileName(storedFileName);
        String downloadName = isBlank(requestedDownloadName) ? safeStoredName : sanitizeFileName(requestedDownloadName);

        Path actorDir = BASE_UPLOAD_DIR.resolve(safeActorId).normalize();
        Path targetFile = actorDir.resolve(safeStoredName).normalize();

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

    private String sanitizeFileName(String fileName) {
        return fileName.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String sanitizePathSegment(String segment) {
        return segment.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record DownloadedFile(Resource resource, String contentType, String downloadName) {
    }
}
