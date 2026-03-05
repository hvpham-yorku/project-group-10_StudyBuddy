package ca.yorku.my.StudyBuddy.daos;

import java.util.List;
import java.util.Optional;

import ca.yorku.my.StudyBuddy.Message;

/**
 * This class is a persistence contract for chat message records.
 */
public interface MessageDAO {
    /**
     * Creates a message in the target chat.
     */
    Message create(String chatId, Message message);

    /**
     * Saves a message by id, creating one when no id is present.
     */
    Message save(String chatId, Message message);

    /**
     * Finds a specific message by chat and message ids.
     */
    Optional<Message> findById(String chatId, String messageId);

    /**
     * Lists messages in descending order with an optional cursor.
     */
    List<Message> listMessages(String chatId, int limit, String beforeCursor);
}
