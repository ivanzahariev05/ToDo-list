package demo.todolist.web;

import demo.todolist.security.JwtUtil;
import demo.todolist.service.UserService;
import demo.todolist.web.dto.LoginRequest;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> loginResponseEntity(@RequestBody @Valid LoginRequest loginRequest,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        String token = jwtUtil.generateToken(authentication.getName());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", token
        ));
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerResponseEntity(@RequestBody @Valid RegisterRequest registerRequest,
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
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "status", "error",
                        "errors", errors
                ));
    }
}