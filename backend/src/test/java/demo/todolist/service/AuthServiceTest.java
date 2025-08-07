package demo.todolist.service;

import demo.todolist.entity.RefreshToken;
import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.security.JwtUtil;
import demo.todolist.web.dto.LoginRequest;
import demo.todolist.web.dto.LoginResponse;
import demo.todolist.web.dto.LogoutRequest;
import demo.todolist.web.dto.RefreshRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;


    @Test
    void login_Success() {
        // given
        LoginRequest rq = new LoginRequest();
        rq.setUsername("john");
        rq.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("john");

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        when(jwtUtil.generateAccessToken(eq("john"), anyCollection()))
                .thenReturn("access123");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setRole(UserRole.USER);
        when(userService.findUser("john")).thenReturn(user);

        RefreshToken refresh = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh123")
                .owner(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenService.createTokenForUser(eq(user), anyLong())).thenReturn(refresh);

        // when
        LoginResponse resp = authService.login(rq);

        // then
        assertEquals("access123", resp.getAccessToken());
        assertEquals("refresh123", resp.getRefreshToken());
    }


    @Test
    void login_BadCredentials() {
        LoginRequest rq = new LoginRequest();
        rq.setUsername("john");
        rq.setPassword("bad");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad"));

        assertThrows(BadCredentialsException.class, () -> authService.login(rq));
    }


    @Test
    void refresh_Success() {
        // given
        RefreshRequest rq = new RefreshRequest();
        rq.setRefreshToken("refresh123");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setRole(UserRole.USER);

        RefreshToken stored = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh123")
                .owner(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenService.findActive("refresh123")).thenReturn(stored);
        when(jwtUtil.generateAccessToken(eq("john"), anyCollection()))
                .thenReturn("newAccess123");

        // when
        LoginResponse resp = authService.refresh(rq);

        // then
        assertEquals("newAccess123", resp.getAccessToken());
        assertEquals("refresh123", resp.getRefreshToken());
    }

    @Test
    void refresh_ExpiredToken() {
        RefreshRequest rq = new RefreshRequest();
        rq.setRefreshToken("expired");

        RefreshToken stored = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("expired")
                .owner(new User())
                .expiresAt(Instant.now().minusSeconds(10))
                .revoked(false)
                .build();
        when(refreshTokenService.findActive("expired")).thenReturn(stored);

        assertThrows(BadCredentialsException.class, () -> authService.refresh(rq));
        verify(refreshTokenService).revoke("expired");
    }


    @Test
    void logout_Success() {
        LogoutRequest rq = new LogoutRequest("refresh123");

        authService.logout(rq);

        verify(refreshTokenService).revoke("refresh123");
    }
}