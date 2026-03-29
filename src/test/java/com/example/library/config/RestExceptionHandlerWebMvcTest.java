//package com.example.library.config;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.example.library.security.JwtAuthFilter;
//
//@WebMvcTest(controllers = RestExceptionHandlerTestController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(RestExceptionHandler.class)
//class RestExceptionHandlerWebMvcTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockBean
//	private JwtAuthFilter jwtAuthFilter;
//
//	/** Scenario: ResponseStatusException becomes JSON error with same HTTP status. */
//	@Test
//	void responseStatusException_mapsToStatusAndBody() throws Exception {
//		mockMvc.perform(get("/__test/errors/status"))
//				.andExpect(status().isNotFound())
//				.andExpect(jsonPath("$.status").value(404))
//				.andExpect(jsonPath("$.message").value("missing"))
//				.andExpect(jsonPath("$.path").value("/__test/errors/status"));
//	}
//
//	/** Scenario: @Valid fails; field errors appear under errors. */
//	@Test
//	void methodArgumentNotValidException_mapsFieldErrors() throws Exception {
//		mockMvc.perform(post("/__test/errors/valid")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content("{}"))
//				.andExpect(status().isBadRequest())
//				.andExpect(jsonPath("$.message").value("Please correct the highlighted fields."))
//				.andExpect(jsonPath("$.errors.name").exists());
//	}
//
//	/** Scenario: DateTimeParseException returns 400 with a clear message. */
//	@Test
//	void dateTimeParseException_returns400() throws Exception {
//		mockMvc.perform(get("/__test/errors/parse"))
//				.andExpect(status().isBadRequest())
//				.andExpect(jsonPath("$.message").value("Invalid date or time format."));
//	}
//}