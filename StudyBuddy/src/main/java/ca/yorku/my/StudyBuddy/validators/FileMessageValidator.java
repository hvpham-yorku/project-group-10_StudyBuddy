package ca.yorku.my.StudyBuddy.validators;

import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;
import ca.yorku.my.StudyBuddy.ValidationException;

public class FileMessageValidator implements MessageValidator {
    @Override
    public String validate(SendMessageDTO request) {
        if (request.getFile() == null) {
            throw new ValidationException("FILE metadata is required");
        }
        if (request.getFile().getFileName() == null || request.getFile().getFileName().isBlank()) {
            throw new ValidationException("FILE fileName is required");
        }
        if (request.getFile().getFileSizeBytes() == null || request.getFile().getFileSizeBytes() <= 0) {
            throw new ValidationException("FILE fileSizeBytes must be greater than 0");
        }
        return (request.getContent() == null || request.getContent().isBlank()) ? "" : request.getContent().trim();
    }
}
