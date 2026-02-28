package ca.yorku.my.StudyBuddy;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreChatDAO implements ChatDAO {

    private static final String CHATS_COLLECTION = "chats";

    @Override
    public Chat create(Chat chat) {
        return save(chat);
    }

    @Override
    public Chat save(Chat chat) {
        if (chat == null || isBlank(chat.getChatId())) {
            throw new ValidationException("chatId is required");
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(CHATS_COLLECTION).document(chat.getChatId());
        try {
            docRef.set(chat).get();
            return chat;
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save chat", exception);
        }
    }

    @Override
    public Optional<Chat> findById(String chatId) {
        if (isBlank(chatId)) {
            return Optional.empty();
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot snapshot = db.collection(CHATS_COLLECTION).document(chatId).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }

            Chat chat = snapshot.toObject(Chat.class);
            if (chat == null) {
                return Optional.empty();
            }

            chat.setChatId(snapshot.getId());
            return Optional.of(chat);
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to load chat", exception);
        }
    }

    @Override
    public Optional<Chat> findDirectByParticipants(String userA, String userB) {
        if (isBlank(userA) || isBlank(userB)) {
            return Optional.empty();
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(CHATS_COLLECTION)
                    .whereEqualTo("type", ChatType.DIRECT.name())
                    .whereArrayContains("participantIds", userA)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Chat chat = document.toObject(Chat.class);
                if (chat == null || chat.getParticipantIds() == null) {
                    continue;
                }

                List<String> participants = new ArrayList<>(chat.getParticipantIds());
                if (participants.size() == 2 && participants.contains(userA) && participants.contains(userB)) {
                    chat.setChatId(document.getId());
                    return Optional.of(chat);
                }
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to query direct chat", exception);
        }
    }

    @Override
    public Optional<Chat> findByRelatedIdAndType(String relatedId, ChatType type) {
        if (isBlank(relatedId) || type == null) {
            return Optional.empty();
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(CHATS_COLLECTION)
                    .whereEqualTo("relatedId", relatedId)
                    .whereEqualTo("type", type.name())
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                return Optional.empty();
            }

            QueryDocumentSnapshot document = documents.get(0);
            Chat chat = document.toObject(Chat.class);
            if (chat == null) {
                return Optional.empty();
            }

            chat.setChatId(document.getId());
            return Optional.of(chat);
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to query event chat", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
