package demo.todolist.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 50, message = "Title cannot be longer than 50 characters")
        String title,

        @Size(max = 250, message = "Description cannot be longer than 250 characters")
        String description,

        boolean isActive
) {}