package demo.todolist.web.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String email,
        String role,
        int tasksDone
) {

}
