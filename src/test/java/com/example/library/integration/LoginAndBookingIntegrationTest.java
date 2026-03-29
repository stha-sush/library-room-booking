package com.example.library.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Full context: login returns jwt cookie, then /api/bookings/mine works with that cookie.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginAndBookingIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	/** Scenario: student1 logs in; jwt cookie is sent on GET /api/bookings/mine. */
	@Test
	void login_setsJwtCookie_thenMineBookings_ok() throws Exception {
		MvcResult login = mockMvc
				.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "username": "student1",
						  "password": "Student@123"
						}
						"""))
				.andExpect(status().isOk()).andExpect(jsonPath("$.username").value("student1")).andReturn();

		var jwt = login.getResponse().getCookie("jwt");
		assertNotNull(jwt);
		assertNotNull(jwt.getValue());

		mockMvc.perform(get("/api/bookings/mine").cookie(jwt)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
