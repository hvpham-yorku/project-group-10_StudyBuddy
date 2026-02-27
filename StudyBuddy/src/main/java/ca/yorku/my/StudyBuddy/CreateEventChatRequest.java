package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

public class CreateEventChatRequest {
    private String chatName;
    private List<String> participantIds = new ArrayList<>();

    public CreateEventChatRequest() {
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }
}
