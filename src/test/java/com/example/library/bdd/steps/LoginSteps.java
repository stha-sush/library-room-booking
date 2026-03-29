package com.example.library.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.repo.BookingRepository;
import com.example.library.repo.UserRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for login.feature.
 *
 * These steps: - create a user in the test database - call the real login
 * endpoint using MockMvc - check the response status and response content
 */
public class LoginSteps {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private MvcResult lastResult;
	private String lastResponseBody;

	@Before
	public void resetDbBeforeScenario() {
		bookingRepository.deleteAll(); // dependent table first
		userRepository.deleteAll();
	}

	@Given("a user {string} exists with password {string} and role {string}")
	public void a_user_exists_with_password_and_role(String username, String rawPassword, String role) {

		UserAccount u = new UserAccount();
		u.setUsername(username);
		u.setFullName("Test User");
		u.setEmail(username + "@test.local");
		u.setRole(UserRole.valueOf(role));

		// Password must be encoded because login compares the hash.
		u.setPasswordHash(passwordEncoder.encode(rawPassword));

		userRepository.save(u);
	}

	@When("a login request is sent with username {string} and password {string}")
	public void a_login_request_is_sent(String username, String password) throws Exception {

		String json = """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);

		lastResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
				.andReturn();

		lastResponseBody = lastResult.getResponse().getContentAsString();
	}

	@Then("the response status should be {int}")
	public void the_response_status_should_be(Integer expectedStatus) {
		int actual = lastResult.getResponse().getStatus();
		assertEquals(expectedStatus.intValue(), actual,
				"Expected status " + expectedStatus + " but got " + actual + ". Response: " + lastResponseBody);
	}

	@Then("the response message should be {string}")
	public void the_response_message_should_be(String expectedMessage) {
		assertNotNull(lastResponseBody);
		assertTrue(lastResponseBody.contains(expectedMessage),
				"Expected message '" + expectedMessage + "' but got: " + lastResponseBody);
	}

	@Then("the response json should contain {string} = {string}")
	public void the_response_json_should_contain(String field, String value) {
		String expected = "\"%s\":\"%s\"".formatted(field, value);
		assertTrue(lastResponseBody.replace(" ", "").contains(expected),
				"Expected JSON to contain " + expected + " but got: " + lastResponseBody);
	}
}