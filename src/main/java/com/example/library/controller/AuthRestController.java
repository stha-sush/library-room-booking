package com.example.library.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.LoginRequest;
import com.example.library.dto.LoginResponse;
import com.example.library.repo.UserRepository;
import com.example.library.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserRepository userRepository;

	public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService,
			UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		var user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		String role = "ROLE_" + user.getRole().name();
		String token = jwtService.generateToken(user.getUsername(), role);

		// Cookie for JWT
		Cookie cookie = new Cookie("jwt", token);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60); // 1 hour
		response.addCookie(cookie);

		response.addHeader("Set-Cookie", "jwt=" + token + "; Max-Age=3600; Path=/; HttpOnly; SameSite=Lax");

		return new LoginResponse(token, user.getUsername(), user.getRole().name());
	}
}