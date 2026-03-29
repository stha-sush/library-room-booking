package com.example.library.repo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.library.model.Booking;
import com.example.library.model.BookingStatus;
import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;
import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private UserRepository userRepository;

	private Room room;
	private UserAccount student;
	private LocalDate date;

	@BeforeEach
	void setUp() {
		date = LocalDate.now().plusDays(7);

		student = new UserAccount();
		student.setUsername("repoStudent");
		student.setPasswordHash("x");
		student.setFullName("Repo Student");
		student.setEmail("repo.student@test.edu");
		student.setRole(UserRole.STUDENT);
		student = userRepository.save(student);

		room = new Room();
		room.setBuilding("B1");
		room.setFloor(1);
		room.setRoomNumber("X1");
		room.setCapacity(10);
		room.setType(RoomType.GROUP);
		room.setProjector(true);
		room.setWhiteboard(true);
		room.setStatus(RoomStatus.ACTIVE);
		room = roomRepository.save(room);
	}

	/** Scenario: no bookings for that room/slot -> no overlap. */
	@Test
	void existsOverlap_noBookings_false() {
		assertFalse(bookingRepository.existsOverlap(room.getId(), date, LocalTime.of(10, 0), LocalTime.of(11, 0)));
	}

	/** Scenario: CONFIRMED booking intersects proposed interval -> true. */
	@Test
	void existsOverlap_confirmedOverlap_true() {
		saveBooking(LocalTime.of(10, 0), LocalTime.of(11, 0), BookingStatus.CONFIRMED);

		assertTrue(bookingRepository.existsOverlap(room.getId(), date, LocalTime.of(10, 30), LocalTime.of(11, 30)));
	}

	/** Scenario: back-to-back slots (end == next start) -> no overlap. */
	@Test
	void existsOverlap_adjacentNonOverlapping_false() {
		saveBooking(LocalTime.of(10, 0), LocalTime.of(11, 0), BookingStatus.CONFIRMED);

		assertFalse(bookingRepository.existsOverlap(room.getId(), date, LocalTime.of(11, 0), LocalTime.of(12, 0)));
	}

	/** Scenario: only CONFIRMED blocks; CANCELLED is ignored. */
	@Test
	void existsOverlap_cancelledDoesNotBlock_false() {
		saveBooking(LocalTime.of(10, 0), LocalTime.of(11, 0), BookingStatus.CANCELLED);

		assertFalse(bookingRepository.existsOverlap(room.getId(), date, LocalTime.of(10, 30), LocalTime.of(11, 30)));
	}

	private void saveBooking(LocalTime start, LocalTime end, BookingStatus status) {
		Booking b = new Booking();
		b.setRoom(room);
		b.setStudent(student);
		b.setBookingDate(date);
		b.setStartTime(start);
		b.setEndTime(end);
		b.setParticipants(2);
		b.setPurpose("Test");
		b.setNote(null);
		b.setStatus(status);
		bookingRepository.save(b);
	}
}
