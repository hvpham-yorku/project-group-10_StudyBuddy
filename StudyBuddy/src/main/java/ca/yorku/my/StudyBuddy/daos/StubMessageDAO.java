package ca.yorku.my.StudyBuddy.daos;

import ca.yorku.my.StudyBuddy.Message;
import ca.yorku.my.StudyBuddy.StubDatabase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("stub")
public class StubMessageDAO implements MessageDAO {

    @Override
    public Message create(String chatId, Message message) {
        message.setMessageId("stub_msg_" + UUID.randomUUID().toString().substring(0, 8));
        message.setChatId(chatId);
        StubDatabase.MESSAGES.add(message);
        return message;
    }

    @Override
    public Message save(String chatId, Message message) {
        StubDatabase.MESSAGES.removeIf(m -> m.getMessageId().equals(message.getMessageId()));
        StubDatabase.MESSAGES.add(message);
        return message;
    }

    @Override
    public Optional<Message> findById(String chatId, String messageId) {
        return StubDatabase.MESSAGES.stream()
                .filter(m -> m.getChatId().equals(chatId) && m.getMessageId().equals(messageId))
                .findFirst();
    }

    @Override
    public Optional<Message> findLatestMessage(String chatId) {
        return StubDatabase.MESSAGES.stream()
                .filter(m -> m.getChatId().equals(chatId))
                .max(Comparator.comparingLong(Message::getTimestampEpochMillis)
                        .thenComparing(Message::getMessageId));
    }

    @Override
    public List<Message> listMessages(String chatId, int limit, String beforeCursor) {
        List<Message> chatMessages = StubDatabase.MESSAGES.stream()
                .filter(m -> m.getChatId().equals(chatId))
                .sorted(Comparator.comparingLong(Message::getTimestampEpochMillis).reversed())
                .collect(Collectors.toList());

        int startIndex = 0;
        if (beforeCursor != null && !beforeCursor.isEmpty()) {
            for (int i = 0; i < chatMessages.size(); i++) {
                if (chatMessages.get(i).getMessageId().equals(beforeCursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }

        int endIndex = Math.min(startIndex + limit, chatMessages.size());
        return chatMessages.subList(startIndex, endIndex);
    }
}