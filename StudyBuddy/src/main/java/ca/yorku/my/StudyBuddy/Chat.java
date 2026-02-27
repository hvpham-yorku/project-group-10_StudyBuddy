package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

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
}
