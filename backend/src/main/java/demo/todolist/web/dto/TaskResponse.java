package demo.todolist.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        boolean isActive,
        LocalDateTime createdAt,
        String ownerUsername
) {}
