package demo.todolist.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final String CLAIM_ROLES = "roles";      // ключът за claim-а

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")   // 900_000 ms (= 15 мин)
    private long accessTtlMillis;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String generateAccessToken(String username,
                                      Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_ROLES, roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTtlMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get(CLAIM_ROLES, List.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;   // невалиден / модифициран токен
        }
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && isTokenValid(token);
    }
}
