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

import com.example.library.config.MethodSecurityTestConfig;
import com.example.library.config.RestExceptionHandler;
import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;
import com.example.library.repo.RoomRepository;
import com.example.library.security.JwtAuthFilter;

@WebMvcTest(AdminRoomRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ RestExceptionHandler.class, MethodSecurityTestConfig.class })
class AdminRoomRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoomRepository roomRepository;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	/** Scenario: admin POST creates room; 201 and saved fields. */
	@Test
	@WithMockUser(roles = "ADMIN")
	void createRoom_admin_returns201() throws Exception {
		when(roomRepository.save(any())).thenAnswer(inv -> {
			Room r = inv.getArgument(0);
			r.setId(42L);
			return r;
		});

		mockMvc.perform(post("/api/admin/rooms").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "building": "North",
				  "floor": 1,
				  "roomNumber": "N100",
				  "capacity": 4,
				  "type": "GROUP",
				  "projector": true,
				  "whiteboard": false,
				  "status": "ACTIVE"
				}
				""")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(42))
				.andExpect(jsonPath("$.building").value("North")).andExpect(jsonPath("$.type").value(RoomType.GROUP.name()))
				.andExpect(jsonPath("$.status").value(RoomStatus.ACTIVE.name()));
	}

	/** Scenario: non-admin is forbidden by @PreAuthorize. */
	@Test
	@WithMockUser(roles = "STUDENT")
	void createRoom_student_returns403() throws Exception {
		mockMvc.perform(post("/api/admin/rooms").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "building": "North",
				  "floor": 1,
				  "roomNumber": "N100",
				  "capacity": 4,
				  "type": "GROUP",
				  "projector": true,
				  "whiteboard": false,
				  "status": "ACTIVE"
				}
				""")).andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Access denied."));
	}
}
