package ca.yorku.my.StudyBuddy;

import java.util.Optional;

/**
 * This class contains the persistance contract for chat documents.
 */
public interface ChatDAO {
    /**
     * Creates a new chat.
     */
    Chat create(Chat chat);

    /**
     * Updates an existing chat or upsert based on implementation behavior.
     */
    Chat save(Chat chat);

    /**
     * Finds a chat by its unique id.
     */
    Optional<Chat> findById(String chatId);

    /**
     * Finds the unique direct chat shared by two participants.
     */
    Optional<Chat> findDirectByParticipants(String userA, String userB);

    /**
     * Finds a chat by related resource id and chat type.
     */
    Optional<Chat> findByRelatedIdAndType(String relatedId, ChatType type);
}
