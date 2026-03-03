package ca.yorku.my.StudyBuddy;

/**
 * This class represents authentication failures such as missing or invalid credentials.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
