package ca.yorku.my.StudyBuddy;

/**
 * This class represents not-found failures for requested resources.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
