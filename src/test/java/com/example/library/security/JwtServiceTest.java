package com.example.library.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private JwtService jwtService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60L);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /** Scenario: token round-trip for username and role claim. */
    @Test
    void generateToken_containsSubjectAndRole_roundTrip() {
        String token = jwtService.generateToken("alice", "ROLE_STUDENT");

        assertEquals("alice", jwtService.extractUsername(token));
        assertEquals("ROLE_STUDENT", jwtService.extractRole(token));
    }

    /** Scenario: expired token is rejected when parsing. */
    @Test
    void extractUsername_expiredToken_throws() {
        Instant past = Instant.now().minusSeconds(120);
        String expired = Jwts.builder()
                .subject("bob")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plusSeconds(60)))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expired));
    }
}