package com.example.library.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.security.JwtAuthFilter;

@WebMvcTest(LogoutRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class LogoutRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	/** Scenario: 204 and Set-Cookie clears jwt. */
	@Test
	void logout_returns204AndClearsCookie() throws Exception {
		var result = mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent()).andReturn();
		assertTrue(result.getResponse().getHeaders("Set-Cookie").stream()
				.anyMatch(h -> h.contains("jwt=") && h.contains("Max-Age=0")));
	}
}
