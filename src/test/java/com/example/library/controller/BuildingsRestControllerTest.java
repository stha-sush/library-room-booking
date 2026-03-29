package com.example.library.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.repo.RoomRepository;
import com.example.library.security.JwtAuthFilter;

@WebMvcTest(BuildingsRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BuildingsRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoomRepository roomRepository;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	/** Scenario: returns distinct building names as JSON array. */
	@Test
	void getBuildings_returnsJsonList() throws Exception {
		when(roomRepository.findDistinctBuildings()).thenReturn(List.of("Main", "Science"));

		mockMvc.perform(get("/api/buildings")).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("[\"Main\",\"Science\"]"));
	}
}
