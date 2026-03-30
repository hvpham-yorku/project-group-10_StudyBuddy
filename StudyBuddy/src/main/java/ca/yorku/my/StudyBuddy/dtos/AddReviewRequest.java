package ca.yorku.my.StudyBuddy.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddReviewRequest(
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    int rating,
    @NotBlank(message = "Review text cannot be blank")
    @Size(min = 5, max = 1000, message = "Review text must be between 5 and 1000 characters")
    String text
) {}
