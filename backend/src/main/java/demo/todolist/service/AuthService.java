package demo.todolist.service;

import demo.todolist.entity.RefreshToken;
import demo.todolist.entity.User;
import demo.todolist.security.JwtUtil;
import demo.todolist.web.dto.LoginRequest;
import demo.todolist.web.dto.LoginResponse;
import demo.todolist.web.dto.LogoutRequest;
import demo.todolist.web.dto.RefreshRequest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final RefreshTokenService refreshService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.access-expiration}")   private long accessTtl;
    @Value("${jwt.refresh-expiration}")  private long refreshTtl;

    @PostConstruct
    void debugTtl() {
        System.out.println("accessTtl = " + accessTtl);
    }

    @Transactional
    public LoginResponse login(LoginRequest rq) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(rq.getUsername(), rq.getPassword()));

        String username = auth.getName();
        Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

        String access = jwtUtil.generateAccessToken(username, roles);

        User owner = userService.findUser(username);
        RefreshToken rt = refreshService.createTokenForUser(owner, refreshTtl);

        return new LoginResponse(access, rt.getToken());
    }


    @Transactional
    public LoginResponse refresh(RefreshRequest rq) {

        RefreshToken stored = refreshService.findActive(rq.getRefreshToken());

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshService.revoke(stored.getToken());
            throw new BadCredentialsException("Refresh token expired");
        }

        User owner = stored.getOwner();
        List<GrantedAuthority> auths = List.of(
                new SimpleGrantedAuthority("ROLE_" + owner.getRole().name()));

        String newAccess = jwtUtil.generateAccessToken(owner.getUsername(), auths);

        return new LoginResponse(newAccess, stored.getToken());
    }

    @Transactional
    public void logout(LogoutRequest rq) {
        refreshService.revoke(rq.refreshToken());
    }
}