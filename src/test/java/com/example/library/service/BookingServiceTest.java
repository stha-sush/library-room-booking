package com.example.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

/**
 * BookingServiceTest
 *
 * This is a UNIT TEST. Test only the business logic inside BookingService.
 *
 * Repositories are mocked so:
 * - no real database is used
 * - tests run fast
 * - we can focus on logic rules (validation, overlap rules, permissions, etc.)
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private UserRepository userRepository;

	// This is the class we actually want to test.
	// Mockito will inject the mocks above into this service.
	@InjectMocks
	private BookingService bookingService;

	/**
	 * Scenario: A student tries to create a booking where the end time is BEFORE
	 * the start time.
	 *
	 * Why this matters:
	 * This is one of the most common validation rules for booking systems.
	 * If it is not enforced, the booking data becomes meaningless.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should throw a ResponseStatusException with HTTP 400 (BAD_REQUEST)
	 * - It should include a friendly message
	 */
	@Test
	void createBooking_shouldReject_whenEndTimeIsBeforeStartTime() {

		// Step 1: Create a request that looks valid except for the time range
		// Date is in the future so it passes the "past date" check.
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(11, 0));
		req.setEndTime(LocalTime.of(10, 0)); // invalid on purpose
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Call the service and confirm it fails as expected
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		// Step 3: Verify the exception contains the correct status + message
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("End time must be after start time.", ex.getReason());
	}

	/**
	 * Scenario: A student tries to create a booking for a past date.
	 *
	 * Why this matters:
	 * The system should never allow booking dates that have already passed.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should throw ResponseStatusException with HTTP 400
	 * - It should include a clear message
	 */
	@Test
	void createBooking_shouldReject_whenBookingDateIsInPast() {

		// Step 1: Create a request with a date in the past
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().minusDays(1)); // invalid on purpose
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Call the service and confirm it fails
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		// Step 3: Verify correct status + message
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Past dates cannot be booked.", ex.getReason());
	}

	/**
	 * Scenario: A student tries to book a room ID that does not exist.
	 *
	 * Why this matters:
	 * The service must reject invalid room references instead of creating broken
	 * bookings.
	 *
	 * Expected result:
	 * - BookingService should throw HTTP 404 NOT_FOUND
	 * - Message should say room was not found
	 */
	@Test
	void createBooking_shouldReject_whenRoomNotFound() {

		// Step 1: Create a valid request structure
		BookingRequest req = new BookingRequest();
		req.setRoomId(99L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Simulate room not existing in repository
		when(roomRepository.findById(99L)).thenReturn(Optional.empty());

		// Step 3: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
		assertEquals("Room not found", ex.getReason());
	}

	/**
	 * Scenario: A student tries to book a room that exists but is not ACTIVE.
	 *
	 * Why this matters:
	 * Inactive rooms should not be available for booking.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should return HTTP 400 BAD_REQUEST
	 */
	@Test
	void createBooking_shouldReject_whenRoomIsNotActive() {

		// Step 1: Create a valid booking request
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Simulate an existing room that is not ACTIVE
		Room room = new Room();
		room.setId(1L);
		room.setCapacity(10);
		room.setStatus(RoomStatus.MAINTENANCE);

		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

		// Step 3: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Room is not active.", ex.getReason());
	}

	/**
	 * Scenario: A student tries to book a room with more participants than the room
	 * can hold.
	 *
	 * Why this matters:
	 * Capacity rules are an important business rule for room booking systems.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should return HTTP 400 BAD_REQUEST
	 */
	@Test
	void createBooking_shouldReject_whenParticipantsExceedCapacity() {

		// Step 1: Create a request with too many participants
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(8); // too many on purpose
		req.setPurpose("Group study");
		req.setNote("N/A");

		// Step 2: Simulate a room with smaller capacity
		Room room = new Room();
		room.setId(1L);
		room.setCapacity(5);
		room.setStatus(RoomStatus.ACTIVE);

		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

		// Step 3: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Participants exceed room capacity.", ex.getReason());
	}

	/**
	 * Scenario: A student tries to book a time slot that overlaps with an existing
	 * confirmed booking.
	 *
	 * Why this matters:
	 * Preventing overlapping bookings is one of the core rules of the system.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should return HTTP 409 CONFLICT
	 */
	@Test
	void createBooking_shouldReject_whenTimeSlotAlreadyBooked() {

		// Step 1: Create a valid request
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Simulate a valid ACTIVE room
		Room room = new Room();
		room.setId(1L);
		room.setCapacity(10);
		room.setStatus(RoomStatus.ACTIVE);

		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

		// Step 3: Simulate overlap found in repository
		when(bookingRepository.existsOverlap(1L, req.getBookingDate(), req.getStartTime(), req.getEndTime()))
				.thenReturn(true);

		// Step 4: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
		assertEquals("Room already booked for this time slot.", ex.getReason());
	}

	/**
	 * Scenario: A booking request passes room checks, but the logged-in user is not
	 * found in the database.
	 *
	 * Why this matters:
	 * A booking must always belong to a real user.
	 *
	 * Expected result:
	 * - BookingService should reject the request
	 * - It should return HTTP 401 UNAUTHORIZED
	 */
	@Test
	void createBooking_shouldReject_whenUserNotFound() {

		// Step 1: Create a valid request
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("N/A");

		// Step 2: Simulate a valid ACTIVE room
		Room room = new Room();
		room.setId(1L);
		room.setCapacity(10);
		room.setStatus(RoomStatus.ACTIVE);

		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		when(bookingRepository.existsOverlap(1L, req.getBookingDate(), req.getStartTime(), req.getEndTime()))
				.thenReturn(false);

		// Step 3: Simulate user not found
		when(userRepository.findByUsername("student1")).thenReturn(Optional.empty());

		// Step 4: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.createBooking("student1", req);
		});

		assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
		assertEquals("User not found", ex.getReason());
	}

	/**
	 * Scenario: A student creates a fully valid booking request.
	 *
	 * Why this matters:
	 * We need to verify the happy path, not only the validation failures.
	 *
	 * Expected result:
	 * - BookingService should create a booking
	 * - Booking should be saved with CONFIRMED status
	 * - Saved object should contain correct room, student, date, and times
	 */
	@Test
	void createBooking_shouldCreateConfirmedBooking_whenRequestIsValid() {

		// Step 1: Create a valid booking request
		BookingRequest req = new BookingRequest();
		req.setRoomId(1L);
		req.setBookingDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setEndTime(LocalTime.of(11, 0));
		req.setParticipants(2);
		req.setPurpose("Study session");
		req.setNote("Quiet room needed");

		// Step 2: Create room and student objects that repositories will return
		Room room = new Room();
		room.setId(1L);
		room.setCapacity(5);
		room.setStatus(RoomStatus.ACTIVE);

		UserAccount user = new UserAccount();
		user.setId(10L);
		user.setUsername("student1");

		// Step 3: Mock repository behaviour for success path
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		when(bookingRepository.existsOverlap(1L, req.getBookingDate(), req.getStartTime(), req.getEndTime()))
				.thenReturn(false);
		when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Step 4: Call the service
		Booking saved = bookingService.createBooking("student1", req);

		// Step 5: Verify the created booking has correct data
		assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
		assertSame(room, saved.getRoom());
		assertSame(user, saved.getStudent());
		assertEquals(req.getBookingDate(), saved.getBookingDate());
		assertEquals(req.getStartTime(), saved.getStartTime());
		assertEquals(req.getEndTime(), saved.getEndTime());
		assertEquals(req.getParticipants(), saved.getParticipants());
		assertEquals(req.getPurpose(), saved.getPurpose());
		assertEquals(req.getNote(), saved.getNote());

		// Step 6: Verify save() was actually called
		verify(bookingRepository).save(any(Booking.class));
	}

	/**
	 * Scenario: A logged-in student wants to view their own bookings.
	 *
	 * Why this matters:
	 * Students should only receive bookings linked to their account.
	 *
	 * Expected result:
	 * - BookingService should return the same list from repository
	 */
	@Test
	void getMyBookings_shouldReturnBookingsForUser() {

		// Step 1: Simulate an existing user
		UserAccount user = new UserAccount();
		user.setId(10L);
		user.setUsername("student1");

		// Step 2: Simulate some bookings already stored for this user
		Booking booking1 = new Booking();
		booking1.setId(1L);

		Booking booking2 = new Booking();
		booking2.setId(2L);

		List<Booking> expectedBookings = List.of(booking1, booking2);

		when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
		when(bookingRepository.findByStudentIdOrderByBookingDateDescStartTimeDesc(10L)).thenReturn(expectedBookings);

		// Step 3: Call the service
		List<Booking> actualBookings = bookingService.getMyBookings("student1");

		// Step 4: Verify the returned list is correct
		assertEquals(expectedBookings, actualBookings);
	}

	/**
	 * Scenario: A request asks for bookings of a username that does not exist.
	 *
	 * Why this matters:
	 * The service should not query bookings for an invalid user.
	 *
	 * Expected result:
	 * - BookingService should throw HTTP 401 UNAUTHORIZED
	 */
	@Test
	void getMyBookings_shouldReject_whenUserNotFound() {

		// Step 1: Simulate missing user
		when(userRepository.findByUsername("student1")).thenReturn(Optional.empty());

		// Step 2: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.getMyBookings("student1");
		});

		assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
		assertEquals("User not found", ex.getReason());
	}

	/**
	 * Scenario: A user tries to cancel a booking ID that does not exist.
	 *
	 * Why this matters:
	 * The service must fail cleanly when invalid booking IDs are used.
	 *
	 * Expected result:
	 * - BookingService should throw HTTP 404 NOT_FOUND
	 */
	@Test
	void cancelBooking_shouldReject_whenBookingNotFound() {

		// Step 1: Simulate missing booking
		when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

		// Step 2: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.cancelBooking("student1", 99L, false);
		});

		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
		assertEquals("Booking not found", ex.getReason());
	}

	/**
	 * Scenario: A user tries to cancel a booking that is not in CONFIRMED status.
	 *
	 * Why this matters:
	 * The business rule allows cancellation only for confirmed bookings.
	 *
	 * Expected result:
	 * - BookingService should throw HTTP 400 BAD_REQUEST
	 */
	@Test
	void cancelBooking_shouldReject_whenBookingIsNotConfirmed() {

		// Step 1: Create an existing booking that is already CANCELLED
		UserAccount user = new UserAccount();
		user.setUsername("student1");

		Booking booking = new Booking();
		booking.setId(1L);
		booking.setStudent(user);
		booking.setStatus(BookingStatus.CANCELLED);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

		// Step 2: Confirm correct exception is thrown
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.cancelBooking("student1", 1L, false);
		});

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Only CONFIRMED bookings can be cancelled.", ex.getReason());
	}

	/**
	 * Scenario: A non-admin user tries to cancel someone else's booking.
	 *
	 * Why this matters:
	 * Students should not be allowed to cancel bookings owned by another student.
	 *
	 * Expected result:
	 * - BookingService should throw HTTP 403 FORBIDDEN
	 */
	@Test
	void cancelBooking_shouldReject_whenUserDoesNotOwnBookingAndIsNotAdmin() {

		// Step 1: Create a confirmed booking that belongs to another student
		UserAccount owner = new UserAccount();
		owner.setUsername("ownerUser");

		Booking booking = new Booking();
		booking.setId(1L);
		booking.setStudent(owner);
		booking.setStatus(BookingStatus.CONFIRMED);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

		// Step 2: Confirm non-owner cannot cancel it
		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
			bookingService.cancelBooking("student1", 1L, false);
		});

		assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
		assertEquals("Not allowed to cancel this booking.", ex.getReason());
	}

	/**
	 * Scenario: The owner of a confirmed booking cancels their own booking.
	 *
	 * Why this matters:
	 * This is the normal cancellation success path for a student.
	 *
	 * Expected result:
	 * - Booking status should change to CANCELLED
	 * - Repository save() should be called
	 */
	@Test
	void cancelBooking_shouldCancel_whenUserOwnsBooking() {

		// Step 1: Create a confirmed booking owned by the requesting user
		UserAccount owner = new UserAccount();
		owner.setUsername("student1");

		Booking booking = new Booking();
		booking.setId(1L);
		booking.setStudent(owner);
		booking.setStatus(BookingStatus.CONFIRMED);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

		// Step 2: Cancel the booking
		bookingService.cancelBooking("student1", 1L, false);

		// Step 3: Verify status changed and save was called
		assertEquals(BookingStatus.CANCELLED, booking.getStatus());
		verify(bookingRepository).save(booking);
	}

	/**
	 * Scenario: An admin cancels another user's confirmed booking.
	 *
	 * Why this matters:
	 * Admins should be allowed to cancel any confirmed booking.
	 *
	 * Expected result:
	 * - Booking status should change to CANCELLED
	 * - Repository save() should be called
	 */
	@Test
	void cancelBooking_shouldCancel_whenRequesterIsAdmin() {

		// Step 1: Create a confirmed booking owned by another user
		UserAccount owner = new UserAccount();
		owner.setUsername("otherStudent");

		Booking booking = new Booking();
		booking.setId(1L);
		booking.setStudent(owner);
		booking.setStatus(BookingStatus.CONFIRMED);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

		// Step 2: Admin cancels the booking
		bookingService.cancelBooking("adminUser", 1L, true);

		// Step 3: Verify status changed and save was called
		assertEquals(BookingStatus.CANCELLED, booking.getStatus());
		verify(bookingRepository).save(booking);
	}
}