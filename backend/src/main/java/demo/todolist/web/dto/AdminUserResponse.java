package demo.todolist.web.dto;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String email,
        String role,
        int tasksDone
) {}
