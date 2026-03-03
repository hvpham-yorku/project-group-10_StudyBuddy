package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the response payload describing active typers in a chat and remaining TTL (time to live) window.
 */
public class TypingStatusResponse {
    private List<String> typingUserIds = new ArrayList<>();
    // This is the time value in milliseconds for how long the typing status is expected to be valid. The client can use this to schedule next status refresh.
    private long expiresInMs;

    public TypingStatusResponse() {
    }

    public List<String> getTypingUserIds() {
        return typingUserIds;
    }

    public void setTypingUserIds(List<String> typingUserIds) {
        this.typingUserIds = typingUserIds;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
