package ca.yorku.my.StudyBuddy.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinEventRequest(
    @NotBlank(message = "Event ID cannot be blank")
    @Size(min = 1, max = 255, message = "Event ID must be between 1 and 255 characters")
    String eventId
) {}
