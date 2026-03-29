package com.example.library.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.config.RestExceptionHandler;
import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.repo.UserRepository;
import com.example.library.security.JwtAuthFilter;
import com.example.library.security.JwtService;

/**
 * This class tests the AuthRestController using MockMvc.
 *
 * Instead of starting the full Spring Boot application, we only load the web
 * layer (controller + MVC components).
 *
 * All dependencies of the controller are mocked so that:
 * - No real database is used
 * - No real JWT logic runs
 * - We can fully control the behaviour in each test
 */
@WebMvcTest(controllers = AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class AuthRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	@Test
	void login_success_returnsTokenAndRole() throws Exception {

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(Mockito.mock(org.springframework.security.core.Authentication.class));

		UserAccount user = new UserAccount();
		user.setUsername("admin");
		user.setRole(UserRole.ADMIN);

		when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
		when(jwtService.generateToken(eq("admin"), eq("ROLE_ADMIN"))).thenReturn("fake-jwt-token");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "admin",
						  "password": "Admin@123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.token").value("fake-jwt-token"))
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(cookie().exists("jwt"));
	}

	@Test
	void login_wrongPassword_returns401FriendlyMessage() throws Exception {

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "admin",
						  "password": "wrong"
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Invalid username or password."));
	}
}