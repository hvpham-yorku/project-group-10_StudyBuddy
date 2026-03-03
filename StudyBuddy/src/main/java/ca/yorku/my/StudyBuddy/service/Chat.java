package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.model.ChatType;
import ca.yorku.my.StudyBuddy.model.LastMessagePreview;
import ca.yorku.my.StudyBuddy.model.MessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a chat aggregate including participants and latest-message preview.
 */
public class Chat {
    private String chatId;
    private ChatType type;
    private String relatedId;
    private String chatName;
    private String chatImage;
    private List<String> participantIds;
    private LastMessagePreview lastMessage;

    public Chat() {
        this.participantIds = new ArrayList<>();
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public LastMessagePreview getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessagePreview lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * This section handles map-based deserialization payloads for lastMessage when data sources
     * do not directly materialize LastMessagePreview instances.
     */
    @SuppressWarnings("unchecked")
    public void setLastMessageFromRaw(Object rawLastMessage) {
        if (rawLastMessage == null) {
            this.lastMessage = null;
            return;
        }

        if (rawLastMessage instanceof LastMessagePreview preview) {
            this.lastMessage = preview;
            return;
        }

        if (rawLastMessage instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            LastMessagePreview preview = new LastMessagePreview();
            Object senderId = map.get("senderId");
            Object content = map.get("content");
            Object type = map.get("type");
            Object timestamp = map.get("timestamp");

            preview.setSenderId(senderId == null ? null : String.valueOf(senderId));
            preview.setContent(content == null ? null : String.valueOf(content));
            preview.setTimestamp(timestamp == null ? null : String.valueOf(timestamp));

            if (type != null) {
                try {
                    preview.setType(MessageType.valueOf(String.valueOf(type)));
                } catch (IllegalArgumentException ignored) {
                    preview.setType(null);
                }
            }

            this.lastMessage = preview;
        }
    }
}
