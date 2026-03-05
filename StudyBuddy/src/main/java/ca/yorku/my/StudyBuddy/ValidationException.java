package ca.yorku.my.StudyBuddy;

/**
 * This class represents request validation failures caused by invalid input data.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
