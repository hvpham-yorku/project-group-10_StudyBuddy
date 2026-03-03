package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = "*")
/**
 * This class handles all the upload and download of chat attachments.
 */
public class UploadController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UploadService uploadService;

    /**
     * Stores an uploaded attachment and returns metadata required for chat messages.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            FileAttachmentDTO response = uploadService.uploadAttachment(file, actorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }
    }

    /**
     * Downloads a previously stored attachment for a specific actor scope.
     */
    @GetMapping("/files/{actorId}/{fileName:.+}")
    public ResponseEntity<?> downloadAttachment(
            @PathVariable String actorId,
            @PathVariable String fileName,
            @RequestParam(value = "downloadName", required = false) String downloadName) {
        try {
            UploadService.DownloadedFile downloadedFile = uploadService.loadAttachment(actorId, fileName, downloadName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(downloadedFile.downloadName())
                                    .build()
                                    .toString())
                    .contentType(MediaType.parseMediaType(downloadedFile.contentType()))
                    .body(downloadedFile.resource());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file");
        }
    }

    /**
     * Returns the shared JSON error shape used by upload endpoints.
     */
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
