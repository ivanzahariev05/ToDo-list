package demo.todolist.service;

import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.exception.DuplicateFieldException;
import demo.todolist.repository.UserRepository;
import demo.todolist.web.dto.RegisterRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password");

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        var response = userService.registerUser(request);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password");

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateFieldException.class, () -> userService.registerUser(request));
    }


    @Test
    void promoteUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setRole(UserRole.USER);

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.promoteUserToAdmin(userId);

        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void promoteUser_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findUserById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.promoteUserToAdmin(userId));
    }


    @Test
    void deleteUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setRole(UserRole.USER);

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenService).revokeAllForUser(userId);

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_AdminNotAllowed() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setRole(UserRole.ADMIN);

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(userId));
    }
}
