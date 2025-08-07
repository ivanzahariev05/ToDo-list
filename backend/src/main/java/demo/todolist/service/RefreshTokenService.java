package demo.todolist.service;

import demo.todolist.entity.RefreshToken;
import demo.todolist.entity.User;
import demo.todolist.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    /* ---------- CRUD ---------- */

    public RefreshToken save(RefreshToken rt) {
        return repo.save(rt);
    }

    public RefreshToken findActive(String token) {
        return repo.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    }

    public void revoke(String token) {                       // при logout
        repo.findByTokenAndRevokedFalse(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }

    public void revokeAllForUser(UUID ownerId) {             // при delete user
        List<RefreshToken> list = repo.findAllByOwnerIdAndRevokedFalse(ownerId);
        list.forEach(rt -> rt.setRevoked(true));
        repo.saveAll(list);
    }

    /* ---------- helpers ---------- */

    public RefreshToken createTokenForUser(User owner, long ttlMillis) {
        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .owner(owner)
                .expiresAt(Instant.now().plusMillis(ttlMillis))
                .revoked(false)
                .build();
        return repo.save(rt);
    }
}
