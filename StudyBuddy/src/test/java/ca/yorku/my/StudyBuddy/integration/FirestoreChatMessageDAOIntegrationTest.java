package ca.yorku.my.StudyBuddy.integration;

import ca.yorku.my.StudyBuddy.*;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FirestoreChatMessageDAOIntegrationTest {

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private MessageDAO messageDAO;

    private String chatId;

    @BeforeEach
    void setup() {
        Path firebaseKey = Path.of("src/main/resources/serviceAccountKey.json");
        Assumptions.assumeTrue(Files.exists(firebaseKey),
                "Skipping Firestore chat/message integration tests because serviceAccountKey.json is missing");

        chatId = "it_chat_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void cleanup() throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        var messagesRef = db.collection("chats").document(chatId).collection("messages");
        var snapshots = messagesRef.get().get().getDocuments();
        for (var snapshot : snapshots) {
            snapshot.getReference().delete().get();
        }

        db.collection("chats").document(chatId).delete().get();
    }

    @Test
    void createAndFindDirectChatByParticipants() {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setType(ChatType.DIRECT);
        chat.setParticipantIds(new ArrayList<>(List.of("u1", "u2")));
        chat.setChatName("Integration Direct Chat");

        chatDAO.create(chat);

        var byId = chatDAO.findById(chatId);
        assertTrue(byId.isPresent());
        assertEquals(ChatType.DIRECT, byId.get().getType());

        var byParticipants = chatDAO.findDirectByParticipants("u1", "u2");
        assertTrue(byParticipants.isPresent());
        assertEquals(chatId, byParticipants.get().getChatId());
    }

    @Test
    void createMessagesAndListWithCursorPagination() {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setType(ChatType.DIRECT);
        chat.setParticipantIds(new ArrayList<>(List.of("u1", "u2")));
        chat.setChatName("Pagination Integration Chat");
        chatDAO.create(chat);

        Message m1 = new Message();
        m1.setChatId(chatId);
        m1.setSenderId("u1");
        m1.setSenderName("u1");
        m1.setContent("msg1");
        m1.setType(MessageType.TEXT);
        m1.setTimestamp("2026-03-01T10:00:00Z");
        m1.setTimestampEpochMillis(1000L);

        Message m2 = new Message();
        m2.setChatId(chatId);
        m2.setSenderId("u1");
        m2.setSenderName("u1");
        m2.setContent("msg2");
        m2.setType(MessageType.TEXT);
        m2.setTimestamp("2026-03-01T10:00:01Z");
        m2.setTimestampEpochMillis(2000L);

        Message m3 = new Message();
        m3.setChatId(chatId);
        m3.setSenderId("u2");
        m3.setSenderName("u2");
        m3.setContent("msg3");
        m3.setType(MessageType.TEXT);
        m3.setTimestamp("2026-03-01T10:00:02Z");
        m3.setTimestampEpochMillis(3000L);

        messageDAO.create(chatId, m1);
        messageDAO.create(chatId, m2);
        Message newest = messageDAO.create(chatId, m3);

        List<Message> firstPage = messageDAO.listMessages(chatId, 2, null);
        assertEquals(2, firstPage.size());
        assertEquals("msg3", firstPage.get(0).getContent());
        assertEquals("msg2", firstPage.get(1).getContent());

        List<Message> secondPage = messageDAO.listMessages(chatId, 2, firstPage.get(1).getMessageId());
        assertEquals(1, secondPage.size());
        assertEquals("msg1", secondPage.get(0).getContent());

        assertNotNull(newest.getMessageId());
    }
}
