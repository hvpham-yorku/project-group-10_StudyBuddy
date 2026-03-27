package ca.yorku.my.StudyBuddy.daos;

import ca.yorku.my.StudyBuddy.Chat;
import ca.yorku.my.StudyBuddy.ChatType;
import ca.yorku.my.StudyBuddy.StubDatabase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Objects;

@Repository
@Profile("stub")
public class StubChatDAO implements ChatDAO {
    @Override
    public Chat create(Chat chat) {
        StubDatabase.CHATS.add(chat);
        return chat;
    }

    @Override
    public Chat save(Chat chat) {
        StubDatabase.CHATS.removeIf(c -> c.getChatId().equals(chat.getChatId()));
        StubDatabase.CHATS.add(chat);
        return chat;
    }

    @Override
    public Optional<Chat> findById(String chatId) {
        return StubDatabase.CHATS.stream()
                .filter(c -> c.getChatId().equals(chatId))
                .findFirst();
    }

    @Override
    public Optional<Chat> findDirectByParticipants(String userA, String userB) {
        return StubDatabase.CHATS.stream()
                .filter(c -> c.getType() == ChatType.DIRECT &&
                        c.getParticipantIds() != null &&
                        c.getParticipantIds().size() == 2 &&
                        c.getParticipantIds().contains(userA) &&
                        c.getParticipantIds().contains(userB))
                .findFirst();
    }

    @Override
    public Optional<Chat> findByRelatedIdAndType(String relatedId, ChatType type) {
        return StubDatabase.CHATS.stream()
                .filter(c -> c.getType() == type && Objects.equals(c.getRelatedId(), relatedId))
                .findFirst();
    }
}