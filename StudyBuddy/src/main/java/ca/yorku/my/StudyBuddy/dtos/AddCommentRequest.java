package ca.yorku.my.StudyBuddy.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, max = 500, message = "Comment text must be between 1 and 500 characters")
    String text
) {}
