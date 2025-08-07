package demo.todolist.web;

import demo.todolist.service.AuthService;
import demo.todolist.service.UserService;
import demo.todolist.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authService.refresh(refreshRequest));
    }


    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        RegisterResponse response = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(
                e -> errors.put(e.getField(), e.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(Map.of("status", "error", "errors", errors));
    }
}
