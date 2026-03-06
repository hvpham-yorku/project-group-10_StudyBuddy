package ca.yorku.my.StudyBuddy.daos;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.Message;
import ca.yorku.my.StudyBuddy.ValidationException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
/**
 * This class is a firestore-backed persistence for chat messages.
 */
@Profile("firestore")
public class FirestoreMessageDAO implements MessageDAO {

    private static final String CHATS_COLLECTION = "chat2";
    private static final String MESSAGES_SUBCOLLECTION = "messages";

    @Override
    /**
     * Creates a new message document and stores generated document id on the model.
     */
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
    /**
     * Saves a message by id, delegating to create when id is missing.
     */
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
    /**
     * Finds one message by chat id + message id.
     */
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
    /**
     * Lists messages in descending chronological order.
     *
     * When beforeCursor is provided, results start strictly after that message
     * in the same sort order (cursor-based page scrolling).
     */
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

    /**
     * Utility blank check used for request validation paths.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
