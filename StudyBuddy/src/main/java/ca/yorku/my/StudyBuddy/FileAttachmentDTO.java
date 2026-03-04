package ca.yorku.my.StudyBuddy;

/**
 * This class contains a data transfer object metadata describing an uploaded chat attachment.
 */
public class FileAttachmentDTO {
    private String fileName;
    private Long fileSizeBytes;
    private String mimeType;
    private String storagePath;

    public FileAttachmentDTO() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
