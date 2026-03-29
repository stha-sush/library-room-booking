package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

	// Booking ID
	private Long id;
	private String bookingDate;

	// Start time (HH:mm format)
	private String startTime;

	// End time (HH:mm format)
	private String endTime;

	// Number of participants
	private int participants;

	// Purpose of booking
	private String purpose;

	// Optional note
	private String note;

	// Booking status (CONFIRMED, CANCELLED, etc.)
	private String status;

	// ID of the booked room
	private Long roomId;

	private String roomLabel;
}