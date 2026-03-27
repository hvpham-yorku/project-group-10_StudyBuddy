package ca.yorku.my.StudyBuddy;

/**
 * Thrown during login when the user has 2FA enabled.
 * Signals the controller to respond with HTTP 202 and that an OTP has been emailed.
 */
public class TwoFARequiredException extends RuntimeException {
    public TwoFARequiredException(String message) {
        super(message);
    }
}
