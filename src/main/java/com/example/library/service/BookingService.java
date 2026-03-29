package com.example.library.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.dto.BookingRequest;
import com.example.library.model.Booking;
import com.example.library.model.BookingStatus;
import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.UserAccount;
import com.example.library.repo.BookingRepository;
import com.example.library.repo.RoomRepository;
import com.example.library.repo.UserRepository;

import jakarta.validation.Valid;

@Service
public class BookingService {

	private final BookingRepository bookingRepository;
	private final RoomRepository roomRepository;
	private final UserRepository userRepository;

	public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
			UserRepository userRepository) {
		this.bookingRepository = bookingRepository;
		this.roomRepository = roomRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public Booking createBooking(String username, @Valid BookingRequest req) {

		// block past date
		if (req.getBookingDate().isBefore(LocalDate.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past dates cannot be booked.");
		}

		LocalTime start = req.getStartTime();
		LocalTime end = req.getEndTime();

		if (!end.isAfter(start)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time.");
		}

		Room room = roomRepository.findById(req.getRoomId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

		if (room.getStatus() != RoomStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is not active.");
		}

		if (req.getParticipants() > room.getCapacity()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participants exceed room capacity.");
		}

		boolean overlap = bookingRepository.existsOverlap(room.getId(), req.getBookingDate(), start, end);

		if (overlap) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already booked for this time slot.");
		}

		UserAccount student = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

		Booking booking = new Booking();
		booking.setRoom(room);
		booking.setStudent(student);
		booking.setBookingDate(req.getBookingDate());
		booking.setStartTime(start);
		booking.setEndTime(end);
		booking.setParticipants(req.getParticipants());
		booking.setPurpose(req.getPurpose());
		booking.setNote(req.getNote());
		booking.setStatus(BookingStatus.CONFIRMED);

		return bookingRepository.save(booking);
	}

	public List<Booking> getMyBookings(String username) {
		UserAccount student = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
		return bookingRepository.findByStudentIdOrderByBookingDateDescStartTimeDesc(student.getId());
	}

	@Transactional
	public void cancelBooking(String username, Long bookingId, boolean isAdmin) {
		Booking b = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

		if (b.getStatus() != BookingStatus.CONFIRMED) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED bookings can be cancelled.");
		}

		if (!isAdmin && !b.getStudent().getUsername().equals(username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to cancel this booking.");
		}

		b.setStatus(BookingStatus.CANCELLED);
		bookingRepository.save(b);
	}
}