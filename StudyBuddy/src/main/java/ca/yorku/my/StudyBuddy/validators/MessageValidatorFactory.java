package ca.yorku.my.StudyBuddy.validators;

import ca.yorku.my.StudyBuddy.MessageType;
import ca.yorku.my.StudyBuddy.ValidationException;
import java.util.Map;
import java.util.EnumMap;

public class MessageValidatorFactory {
    
    private static final Map<MessageType, MessageValidator> validators = new EnumMap<>(MessageType.class);

    // This static block automatically registers our validators when the app starts
    static {
        validators.put(MessageType.TEXT, new TextMessageValidator());
        validators.put(MessageType.LINK, new LinkMessageValidator());
        validators.put(MessageType.FILE, new FileMessageValidator());
    }

    public static MessageValidator getValidator(MessageType type) {
        MessageValidator validator = validators.get(type);
        if (validator == null) {
            throw new ValidationException("Unsupported message type");
        }
        return validator;
    }
}
