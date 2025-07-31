package demo.todolist.utils;

import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.RegisterResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .tasksDone(0)
                .role(UserRole.USER)
                .build();
    }

    public RegisterResponse toResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getTasksDone()
        );
    }
}