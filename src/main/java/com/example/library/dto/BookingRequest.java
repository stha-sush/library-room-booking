package com.example.library.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookingRequest
 *
 * DTO used when a student creates a booking. This object is received as JSON in
 * REST API calls.
 *
 * Validation annotations ensure request correctness before reaching business
 * logic.
 */
@Data
@NoArgsConstructor
public class BookingRequest {

	/**
	 * ID of the room being booked. Must not be null.
	 */
	@NotNull(message = "Room ID is required")
	private Long roomId;

	/**
	 * Booking date. Must be today or a future date.
	 */
	@NotNull(message = "Booking date is required")
	@FutureOrPresent(message = "must be today or a future date")
	private LocalDate bookingDate;

	/**
	 * Booking start time.
	 */
	@NotNull(message = "Start time is required")
	private LocalTime startTime;

	/**
	 * Booking end time.
	 */
	@NotNull(message = "End time is required")
	private LocalTime endTime;

	/**
	 * Number of participants. Must be between 1 and 50.
	 */
	@Min(value = 1, message = "must be at least 1")
	@Max(value = 50, message = "must be 50 or less")
	private int participants;

	/**
	 * Purpose of booking (e.g., Study, Meeting). Cannot be blank.
	 */
	@NotBlank(message = "Purpose is required")
	@Size(max = 60, message = "must be at most 60 characters")
	private String purpose;

	/**
	 * Optional additional notes.
	 */
	@Size(max = 300, message = "must be at most 300 characters")
	private String note;
}