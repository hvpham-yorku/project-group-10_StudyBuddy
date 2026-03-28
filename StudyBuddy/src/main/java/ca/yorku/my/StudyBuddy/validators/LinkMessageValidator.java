package ca.yorku.my.StudyBuddy.validators;

import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;
import ca.yorku.my.StudyBuddy.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;

public class LinkMessageValidator implements MessageValidator {
    @Override
    public String validate(SendMessageDTO request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ValidationException("LINK message content is required");
        }
        String link = request.getContent().trim();
        if (!isValidHttpUrl(link)) {
            throw new ValidationException("LINK content must be a valid http(s) URL");
        }
        return link;
    }

    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && 
                   uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
