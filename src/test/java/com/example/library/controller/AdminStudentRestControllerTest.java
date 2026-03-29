package com.example.library.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.config.RestExceptionHandler;
import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.security.JwtAuthFilter;
import com.example.library.service.UserService;

@WebMvcTest(AdminStudentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class AdminStudentRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	/** Scenario: happy path; 200 and StudentResponse JSON. */
	@Test
	@WithMockUser(roles = "ADMIN")
	void createStudent_success_returns200() throws Exception {
		UserAccount u = new UserAccount();
		u.setId(7L);
		u.setUsername("newbie");
		u.setFullName("New Student");
		u.setEmail("newbie@univ.edu");
		u.setRole(UserRole.STUDENT);

		when(userService.createStudent(any())).thenReturn(u);

		mockMvc.perform(post("/api/admin/students").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "username": "newbie",
				  "fullName": "New Student",
				  "email": "newbie@univ.edu",
				  "tempPassword": "TempPass12"
				}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7)).andExpect(jsonPath("$.username").value("newbie"))
				.andExpect(jsonPath("$.role").value("STUDENT"));
	}

	/** Scenario: service throws for duplicate username -> 400 from handler. */
	@Test
	@WithMockUser(roles = "ADMIN")
	void createStudent_duplicateUsername_returns400() throws Exception {
		when(userService.createStudent(any())).thenThrow(new IllegalArgumentException("Username already exists"));

		mockMvc.perform(post("/api/admin/students").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "username": "dup",
				  "fullName": "Dup User",
				  "email": "dup@univ.edu",
				  "tempPassword": "TempPass12"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Username already exists"));
	}
}
