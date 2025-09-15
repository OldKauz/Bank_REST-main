package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "mysecretkeymysecretkeymysecretkey123"; // лучше вынести в application.yml
    private static final long EXPIRATION_TIME = 1000 * 60 * 15; // 15 минут

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Создать токен
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) // sub: имя пользователя
                .claim("role", role)  // кастомное поле
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Извлечь username
    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    // Извлечь роль
    public String extractRole(String token) {
        return (String) parseClaims(token).getBody().get("role");
    }

    // Проверить, истёк ли токен
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getBody().getExpiration().before(new Date());
    }

    // Проверка токена
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
