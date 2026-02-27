package ca.yorku.my.StudyBuddy;

import java.util.Optional;

public interface ChatDAO {
    Chat create(Chat chat);
    Chat save(Chat chat);
    Optional<Chat> findById(String chatId);
    Optional<Chat> findDirectByParticipants(String userA, String userB);
    Optional<Chat> findByRelatedIdAndType(String relatedId, ChatType type);
}
