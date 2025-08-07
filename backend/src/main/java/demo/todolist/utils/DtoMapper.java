package demo.todolist.utils;

import demo.todolist.entity.Task;
import demo.todolist.entity.User;
import demo.todolist.web.dto.TaskRequest;
import demo.todolist.web.dto.TaskResponse;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.RegisterResponse;
import demo.todolist.web.dto.ErrorResponse;
import demo.todolist.entity.UserRole;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@UtilityClass
public class DtoMapper {

    public User toUserEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .tasksDone(0)
                .role(UserRole.USER)
                .build();
    }

    public RegisterResponse toUserResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getTasksDone()
        );
    }

    public Task toTaskEntity(TaskRequest request, User owner) {
        return Task.builder()
                .title(request.title())
                .description(request.description())
                .isActive(request.isActive())
                .createdAt(LocalDateTime.now())
                .owner(owner)
                .build();
    }

    public TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isActive(),
                task.getCreatedAt(),
                task.getOwner().getUsername()
        );
    }

    // --- ERROR mapping ---
    public ErrorResponse toErrorResponse(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
