package com.example.library.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.BookingRequest;
import com.example.library.dto.BookingResponse;
import com.example.library.model.Booking;
import com.example.library.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingRestController {

	// Business logic is handled in service layer
	private final BookingService bookingService;

	// Create a new booking (STUDENT or ADMIN)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED) // 201 for REST create
	public BookingResponse create(@Valid @RequestBody BookingRequest req, @AuthenticationPrincipal UserDetails user) {

		Booking booking = bookingService.createBooking(user.getUsername(), req);
		return toDto(booking);
	}

	// View logged-in user's bookings
	@GetMapping("/mine")
	public List<BookingResponse> mine(@AuthenticationPrincipal UserDetails user) {
		return bookingService.getMyBookings(user.getUsername()).stream().map(this::toDto).toList();
	}

	// Cancel booking (owner OR admin)
	@DeleteMapping("/{id}")
	public void cancel(@PathVariable Long id, Authentication auth) {
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

		bookingService.cancelBooking(auth.getName(), id, isAdmin);
	}

	// Convert entity -> response DTO (never return entity directly)
	private BookingResponse toDto(Booking b) {
		String label = b.getRoom().getBuilding() + " " + b.getRoom().getRoomNumber();

		return BookingResponse.builder().id(b.getId()).bookingDate(b.getBookingDate().toString())
				.startTime(b.getStartTime().toString()).endTime(b.getEndTime().toString())
				.participants(b.getParticipants()).purpose(b.getPurpose()).note(b.getNote())
				.status(b.getStatus().name()).roomId(b.getRoom().getId()).roomLabel(label).build();
	}
}