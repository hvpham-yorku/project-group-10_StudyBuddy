package ca.yorku.my.StudyBuddy.validators;

import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;
import ca.yorku.my.StudyBuddy.ValidationException;

public interface MessageValidator {
    
    /**
     * Validates the message payload and returns the normalized content string.
     */
    String validate(SendMessageDTO request) throws ValidationException;
}
