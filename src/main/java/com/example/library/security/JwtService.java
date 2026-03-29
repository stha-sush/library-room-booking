package com.example.library.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expMinutes;

	public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expMinutes}") long expMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expMinutes = expMinutes;
	}

	public String generateToken(String username, String role) {
		Instant now = Instant.now();
		Instant exp = now.plus(expMinutes, ChronoUnit.MINUTES);

		return Jwts.builder().subject(username).claim("role", role) // ROLE_ADMIN / ROLE_STUDENT
				.issuedAt(Date.from(now)).expiration(Date.from(exp)).signWith(key).compact();
	}

	public String extractUsername(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public String extractRole(String token) {
		Object v = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role");
		return v == null ? null : v.toString();
	}
}
