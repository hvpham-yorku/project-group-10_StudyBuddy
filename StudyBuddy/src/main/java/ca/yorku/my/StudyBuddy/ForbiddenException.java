package ca.yorku.my.StudyBuddy;

/**
 * This class represents authorization failures where the actor is authenticated
 * but not allowed to perform the requested action.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
