package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreMessageDAO implements MessageDAO {

    private static final String CHATS_COLLECTION = "chats";
    private static final String MESSAGES_SUBCOLLECTION = "messages";

    @Override
    public Message create(String chatId, Message message) {
        if (isBlank(chatId) || message == null) {
            throw new ValidationException("chatId and message are required");
        }

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference messagesRef = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION);
        try {
            DocumentReference documentReference = messagesRef.add(message).get();
            message.setMessageId(documentReference.getId());
            message.setChatId(chatId);
            documentReference.set(message).get();
            return message;
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create message", exception);
        }
    }

    @Override
    public Message save(String chatId, Message message) {
        if (message == null || isBlank(message.getMessageId())) {
            return create(chatId, message);
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(message.getMessageId());
        try {
            docRef.set(message).get();
            return message;
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save message", exception);
        }
    }

    @Override
    public Optional<Message> findById(String chatId, String messageId) {
        if (isBlank(chatId) || isBlank(messageId)) {
            return Optional.empty();
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(messageId);
        try {
            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }

            Message message = snapshot.toObject(Message.class);
            if (message == null) {
                return Optional.empty();
            }
            message.setMessageId(snapshot.getId());
            message.setChatId(chatId);
            return Optional.of(message);
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find message", exception);
        }
    }

    @Override
    public List<Message> listMessages(String chatId, int limit, String beforeCursor) {
        if (isBlank(chatId)) {
            return new ArrayList<>();
        }

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference messagesRef = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION);

        Query query = messagesRef
                .orderBy("timestampEpochMillis", Query.Direction.DESCENDING)
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                .limit(limit);

        try {
            if (!isBlank(beforeCursor)) {
                DocumentSnapshot beforeDoc = messagesRef.document(beforeCursor).get().get();
                if (!beforeDoc.exists()) {
                    throw new ValidationException("before cursor does not exist in this chat");
                }

                Message beforeMessage = beforeDoc.toObject(Message.class);
                if (beforeMessage == null) {
                    throw new ValidationException("before cursor does not exist in this chat");
                }

                query = query.startAfter(beforeMessage.getTimestampEpochMillis(), beforeDoc.getId());
            }

            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Message message = document.toObject(Message.class);
                if (message == null) {
                    continue;
                }
                message.setMessageId(document.getId());
                message.setChatId(chatId);
                messages.add(message);
            }

            return messages;
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to list messages", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
