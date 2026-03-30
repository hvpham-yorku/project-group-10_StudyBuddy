package ca.yorku.my.StudyBuddy.dtos;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating a new event.
 * Separate from EventResponseDTO to enforce input validation.
 */
public record EventCreateRequest(
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    String title,

    @NotBlank(message = "Course cannot be blank")
    @Size(min = 1, max = 255, message = "Course must be between 1 and 255 characters")
    String course,

    @NotBlank(message = "Location cannot be blank")
    @Size(min = 1, max = 255, message = "Location must be between 1 and 255 characters")
    String location,

    @NotBlank(message = "Date cannot be blank")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
    String date,

    @NotBlank(message = "Time cannot be blank")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in format HH:MM")
    String time,

    @Positive(message = "Duration must be positive")
    @Max(value = 480, message = "Duration must not exceed 480 minutes (8 hours)")
    int duration,

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    String description,

    @Positive(message = "Max participants must be positive")
    @Max(value = 500, message = "Max participants must not exceed 500")
    int maxParticipants,

    // Optional fields - can be null or empty
    List<String> tags
) {}
