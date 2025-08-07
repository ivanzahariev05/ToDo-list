package demo.todolist.service;

import demo.todolist.entity.RefreshToken;
import demo.todolist.entity.User;
import demo.todolist.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;

    @InjectMocks
    private RefreshTokenService service;


    @Test
    void createToken_Success() {
        User user = buildUser();
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken rt = service.createTokenForUser(user, 3600L);

        assertEquals(user, rt.getOwner());
        assertNotNull(rt.getToken());
        verify(repo).save(any(RefreshToken.class));
    }


    @Test
    void findActive_Success() {
        RefreshToken rt = buildToken(false, Instant.now().plusSeconds(60));
        when(repo.findByTokenAndRevokedFalse("abc")).thenReturn(Optional.of(rt));

        RefreshToken result = service.findActive("abc");
        assertSame(rt, result);
    }

    @Test
    void findActive_Invalid() {
        when(repo.findByTokenAndRevokedFalse("missing")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> service.findActive("missing"));
    }


    @Test
    void revoke_Success() {
        RefreshToken rt = buildToken(false, Instant.now().plusSeconds(60));
        when(repo.findByTokenAndRevokedFalse("abc")).thenReturn(Optional.of(rt));
        when(repo.save(rt)).thenReturn(rt);

        service.revoke("abc");

        assertTrue(rt.isRevoked());
        verify(repo).save(rt);
    }


    @Test
    void revokeAllForUser_Success() {
        UUID ownerId = UUID.randomUUID();
        RefreshToken t1 = buildToken(false, Instant.now().plusSeconds(60));
        RefreshToken t2 = buildToken(false, Instant.now().plusSeconds(120));
        when(repo.findAllByOwnerIdAndRevokedFalse(ownerId)).thenReturn(List.of(t1, t2));
        when(repo.saveAll(anyList())).thenReturn(List.of(t1, t2));

        service.revokeAllForUser(ownerId);

        assertTrue(t1.isRevoked());
        assertTrue(t2.isRevoked());
        verify(repo).saveAll(anyList());
    }


    private User buildUser() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("user" + u.getId().toString().substring(0, 5));
        return u;
    }

    private RefreshToken buildToken(boolean revoked, Instant expiresAt) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .owner(buildUser())
                .expiresAt(expiresAt)
                .revoked(revoked)
                .build();
    }
}
