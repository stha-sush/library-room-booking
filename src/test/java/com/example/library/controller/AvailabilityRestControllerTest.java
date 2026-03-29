package com.example.library.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.config.RestExceptionHandler;
import com.example.library.dto.RoomResponse;
import com.example.library.security.JwtAuthFilter;
import com.example.library.service.RoomService;

@WebMvcTest(AvailabilityRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class AvailabilityRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoomService roomService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	/** Scenario: future date + times; 200 and JSON from RoomService. */
	@Test
	void available_validParams_returns200() throws Exception {
		String d = LocalDate.now().plusDays(1).toString();
		when(roomService.findAvailable(d, "10:00", "11:00", null, null, null))
				.thenReturn(List.of(new RoomResponse(1L, "Main", 1, "R1", 4, "GROUP", true, true)));

		mockMvc.perform(get("/api/availability").param("date", d).param("start", "10:00").param("end", "11:00"))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].building").value("Main"));
	}

	/** Scenario: search date before today is rejected. */
	@Test
	void available_pastDate_returns400() throws Exception {
		mockMvc.perform(
				get("/api/availability").param("date", "2000-01-01").param("start", "10:00").param("end", "11:00"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Past dates cannot be searched."));
	}

	/** Scenario: date param not parseable -> 400 from DateTimeParseException handler. */
	@Test
	void available_invalidDateFormat_returns400() throws Exception {
		mockMvc.perform(
				get("/api/availability").param("date", "not-a-date").param("start", "10:00").param("end", "11:00"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid date or time format."));
	}
}
