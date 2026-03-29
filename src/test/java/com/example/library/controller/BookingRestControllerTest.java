//package com.example.library.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.example.library.dto.BookingRequest;
//import com.example.library.model.Booking;
//import com.example.library.model.BookingStatus;
//import com.example.library.model.Room;
//import com.example.library.security.JwtAuthFilter;
//import com.example.library.service.BookingService;
//
//@WebMvcTest(BookingRestController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class BookingRestControllerTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockBean
//	private BookingService bookingService;
//
//	@MockBean
//	private JwtAuthFilter jwtAuthFilter;
//
//	/** Scenario: POST create booking returns 201 and JSON body from service result. */
//	@Test
//	@WithMockUser(username = "student1", roles = "STUDENT")
//	void createBooking_returns201AndJson() throws Exception {
//		Room room = new Room();
//		room.setId(1L);
//		room.setBuilding("Main");
//		room.setRoomNumber("R101");
//
//		Booking b = new Booking();
//		b.setId(99L);
//		b.setRoom(room);
//		b.setBookingDate(LocalDate.now().plusDays(1));
//		b.setStartTime(LocalTime.of(10, 0));
//		b.setEndTime(LocalTime.of(11, 0));
//		b.setParticipants(2);
//		b.setPurpose("Study");
//		b.setNote(null);
//		b.setStatus(BookingStatus.CONFIRMED);
//
//		when(bookingService.createBooking(eq("student1"), any(BookingRequest.class))).thenReturn(b);
//
//		mockMvc.perform(post("/api/bookings")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content("""
//						{
//						  "roomId": 1,
//						  "bookingDate": "%s",
//						  "startTime": "10:00:00",
//						  "endTime": "11:00:00",
//						  "participants": 2,
//						  "purpose": "Study"
//						}
//						""".formatted(LocalDate.now().plusDays(1))))
//				.andExpect(status().isCreated())
//				.andExpect(jsonPath("$.id").value(99))
//				.andExpect(jsonPath("$.roomLabel").value("Main R101"))
//				.andExpect(jsonPath("$.status").value("CONFIRMED"));
//
//		verify(bookingService).createBooking(eq("student1"), any(BookingRequest.class));
//	}
//
//	/** Scenario: GET /mine returns list of booking DTOs. */
//	@Test
//	@WithMockUser(username = "student1", roles = "STUDENT")
//	void mine_returnsBookings() throws Exception {
//		Room room = new Room();
//		room.setId(1L);
//		room.setBuilding("A");
//		room.setRoomNumber("1");
//
//		Booking b = new Booking();
//		b.setId(1L);
//		b.setRoom(room);
//		b.setBookingDate(LocalDate.now().plusDays(1));
//		b.setStartTime(LocalTime.of(9, 0));
//		b.setEndTime(LocalTime.of(10, 0));
//		b.setParticipants(1);
//		b.setPurpose("Read");
//		b.setNote("");
//		b.setStatus(BookingStatus.CONFIRMED);
//
//		when(bookingService.getMyBookings("student1")).thenReturn(List.of(b));
//
//		mockMvc.perform(get("/api/bookings/mine"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$[0].id").value(1))
//				.andExpect(jsonPath("$[0].roomLabel").value("A 1"));
//	}
//
//	/** Scenario: student cancels; service gets isAdmin=false. */
//	@Test
//	@WithMockUser(username = "student1", roles = "STUDENT")
//	void cancel_asStudent_callsServiceWithAdminFalse() throws Exception {
//		doNothing().when(bookingService).cancelBooking("student1", 5L, false);
//
//		mockMvc.perform(delete("/api/bookings/5"))
//				.andExpect(status().isOk());
//
//		verify(bookingService).cancelBooking("student1", 5L, false);
//	}
//
//	/** Scenario: admin cancels; service gets isAdmin=true. */
//	@Test
//	@WithMockUser(username = "admin", roles = "ADMIN")
//	void cancel_asAdmin_callsServiceWithAdminTrue() throws Exception {
//		doNothing().when(bookingService).cancelBooking("admin", 5L, true);
//
//		mockMvc.perform(delete("/api/bookings/5"))
//				.andExpect(status().isOk());
//
//		verify(bookingService).cancelBooking("admin", 5L, true);
//	}
//}