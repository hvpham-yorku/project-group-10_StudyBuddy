package ca.yorku.my.StudyBuddy.validators;

import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;
import ca.yorku.my.StudyBuddy.ValidationException;

public class TextMessageValidator implements MessageValidator {
    @Override
    public String validate(SendMessageDTO request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ValidationException("TEXT message content is required");
        }
        return request.getContent().trim();
    }
}
