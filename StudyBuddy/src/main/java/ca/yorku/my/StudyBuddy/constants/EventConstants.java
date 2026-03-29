package ca.yorku.my.StudyBuddy.constants;

/**
 * Constants used throughout the Event management system.
 * Centralizes hard-coded values for easier maintenance and configuration.
 */
public final class EventConstants {
    
    // Prevent instantiation
    private EventConstants() {
    }

    // ===== ID Prefixes =====
    public static final String EVENT_ID_PREFIX = "event_";
    public static final String REVIEW_ID_PREFIX = "r_";
    public static final String COMMENT_ID_PREFIX = "c_";

    // ===== Event Status Constants =====
    public static final String EVENT_STATUS_ACTIVE = "ACTIVE";
    public static final String EVENT_STATUS_CANCELLED = "CANCELLED";
    public static final String EVENT_STATUS_COMPLETED = "COMPLETED";

    // ===== Authorization Constants =====
    public static final String BEARER_PREFIX = "Bearer ";

    // ===== Error Messages =====
    public static final String ERROR_EVENT_NOT_FOUND = "Event not found";
    public static final String ERROR_REVIEW_NOT_FOUND = "Event or Review not found";
    public static final String ERROR_FAILED_TO_JOIN = "Failed to join event";
    public static final String ERROR_FAILED_TO_LEAVE = "Failed to remove from event";
    public static final String ERROR_ONLY_HOST_CAN_KICK = "Only the host can kick users.";
    public static final String ERROR_SERVER_GENERAL = "Server Error: ";
    public static final String ERROR_SECURITY_ALERT_FORMAT = "SECURITY ALERT: User %s attempted to kick %s";

    // ===== Success Messages =====
    public static final String SUCCESS_JOINED_EVENT = "Joined Event!";
    public static final String SUCCESS_LEFT_EVENT = "Successfully removed from event!";

    // ===== Validation Constants =====
    public static final int MAX_EVENT_DURATION_MINUTES = 480;  // 8 hours
    public static final int MAX_PARTICIPANTS = 500;
    public static final int MAX_REVIEW_LENGTH = 1000;
    public static final int MAX_COMMENT_LENGTH = 500;
    public static final int MIN_REVIEW_LENGTH = 5;
    public static final int MIN_TITLE_LENGTH = 3;
    public static final int MIN_DESCRIPTION_LENGTH = 10;

    // ===== Review Rating Bounds =====
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
}
