package demo.todolist.web;

import demo.todolist.service.UserService;
import demo.todolist.web.dto.AdminUserResponse;
import demo.todolist.web.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("{id}/promote")
    public RegisterResponse promote(@PathVariable UUID id) {
        return userService.promoteUserToAdmin(id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @GetMapping
    public List<AdminUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

}
