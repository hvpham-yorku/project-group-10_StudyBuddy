package ca.yorku.my.StudyBuddy.dtos;

import ca.yorku.my.StudyBuddy.MessageType;

/**
 * This class is a request payload for sending chat messages of multiple supported types.
 */
public class SendMessageDTO {
    private String chatId;
    private String content;
    private MessageType type;
    private FileAttachmentDTO file;

    public SendMessageDTO() {
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public FileAttachmentDTO getFile() {
        return file;
    }

    public void setFile(FileAttachmentDTO file) {
        this.file = file;
    }
}
