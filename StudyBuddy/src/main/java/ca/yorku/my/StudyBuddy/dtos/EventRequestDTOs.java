package ca.yorku.my.StudyBuddy.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for joining an event.
 */
public record JoinEventRequest(
    @NotBlank(message = "Event ID cannot be blank")
    @Size(min = 1, max = 255, message = "Event ID must be between 1 and 255 characters")
    String eventId
) {}

/**
 * Request DTO for leaving an event.
 */
public record LeaveEventRequest(
    @NotBlank(message = "Event ID cannot be blank")
    @Size(min = 1, max = 255, message = "Event ID must be between 1 and 255 characters")
    String eventId,
    @NotBlank(message = "User ID cannot be blank")
    @Size(min = 1, max = 255, message = "User ID must be between 1 and 255 characters")
    String userId
) {}

/**
 * Request DTO for adding a review to an event.
 */
public record AddReviewRequest(
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    int rating,
    @NotBlank(message = "Review text cannot be blank")
    @Size(min = 5, max = 1000, message = "Review text must be between 5 and 1000 characters")
    String text
) {}

/**
 * Request DTO for adding a comment to a review.
 */
public record AddCommentRequest(
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, max = 500, message = "Comment text must be between 1 and 500 characters")
    String text
) {}

