package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.Chat;
import ca.yorku.my.StudyBuddy.LastMessagePreview;
import ca.yorku.my.StudyBuddy.MessageType;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Chat model conversion behavior around last-message payloads.
 */
class ChatModelUnitTests {

    /**
     * Verifies map payloads are converted into LastMessagePreview with all fields preserved.
     */
    @Test
    void setLastMessageFromMapConvertsFieldsSafely() {
        Chat chat = new Chat();

        chat.setLastMessageFromRaw(Map.of(
                "senderId", "u1",
                "content", "hello",
                "type", "TEXT",
                "timestamp", "2026-03-03T12:00:00Z"
        ));

        LastMessagePreview preview = chat.getLastMessage();
        assertNotNull(preview);
        assertEquals("u1", preview.getSenderId());
        assertEquals("hello", preview.getContent());
        assertEquals(MessageType.TEXT, preview.getType());
        assertEquals("2026-03-03T12:00:00Z", preview.getTimestamp());
    }

    /**
     * Verifies unknown message types do not throw and are normalized to null type.
     */
    @Test
    void setLastMessageFromMapAllowsUnknownTypeWithoutThrowing() {
        Chat chat = new Chat();

        chat.setLastMessageFromRaw(Map.of(
                "senderId", "u2",
                "content", "file",
                "type", "UNKNOWN_TYPE",
                "timestamp", "2026-03-03T12:05:00Z"
        ));

        LastMessagePreview preview = chat.getLastMessage();
        assertNotNull(preview);
        assertNull(preview.getType());
        assertEquals("u2", preview.getSenderId());
    }

    /**
     * Verifies passing a preview object keeps the same reference in the model.
     */
    @Test
    void setLastMessageFromPreviewObjectStoresSameInstance() {
        Chat chat = new Chat();
        LastMessagePreview preview = new LastMessagePreview("u3", "ping", MessageType.TEXT, "2026-03-03T12:10:00Z");

        chat.setLastMessageFromRaw(preview);

        assertSame(preview, chat.getLastMessage());
    }
}
