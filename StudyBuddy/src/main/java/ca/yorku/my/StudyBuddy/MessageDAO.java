package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Optional;

public interface MessageDAO {
    Message create(String chatId, Message message);
    Message save(String chatId, Message message);
    Optional<Message> findById(String chatId, String messageId);
    List<Message> listMessages(String chatId, int limit, String beforeCursor);
}
