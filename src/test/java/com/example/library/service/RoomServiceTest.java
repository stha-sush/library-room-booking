package com.example.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.library.dto.RoomResponse;
import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;
import com.example.library.repo.BookingRepository;
import com.example.library.repo.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private RoomService roomService;

	/** Scenario: only ACTIVE rooms are included. */
	@Test
	void findAvailable_shouldReturnOnlyActiveRooms() {
		Room activeRoom = new Room();
		activeRoom.setId(1L);
		activeRoom.setStatus(RoomStatus.ACTIVE);

		Room inactiveRoom = new Room();
		inactiveRoom.setId(2L);
		inactiveRoom.setStatus(RoomStatus.MAINTENANCE);

		when(roomRepository.findAll()).thenReturn(List.of(activeRoom, inactiveRoom));
		when(bookingRepository.existsOverlap(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
				.thenReturn(false);

		List<RoomResponse> result = roomService.findAvailable(LocalDate.now().plusDays(1).toString(), "10:00", "11:00",
				null, null, null);

		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).id());
	}

	/** Scenario: filter by building name. */
	@Test
	void findAvailable_shouldFilterByBuilding() {
		Room room1 = new Room();
		room1.setId(1L);
		room1.setStatus(RoomStatus.ACTIVE);
		room1.setBuilding("A");

		Room room2 = new Room();
		room2.setId(2L);
		room2.setStatus(RoomStatus.ACTIVE);
		room2.setBuilding("B");

		when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
		when(bookingRepository.existsOverlap(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
				.thenReturn(false);

		List<RoomResponse> result = roomService.findAvailable(LocalDate.now().plusDays(1).toString(), "10:00", "11:00",
				null, null, "A");

		assertEquals(1, result.size());
		assertEquals("A", result.get(0).building());
	}

	/** Scenario: minimum capacity filter. */
	@Test
	void findAvailable_shouldFilterByCapacity() {
		Room smallRoom = new Room();
		smallRoom.setId(1L);
		smallRoom.setStatus(RoomStatus.ACTIVE);
		smallRoom.setCapacity(2);

		Room bigRoom = new Room();
		bigRoom.setId(2L);
		bigRoom.setStatus(RoomStatus.ACTIVE);
		bigRoom.setCapacity(10);

		when(roomRepository.findAll()).thenReturn(List.of(smallRoom, bigRoom));
		when(bookingRepository.existsOverlap(2L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
				.thenReturn(false);

		List<RoomResponse> result = roomService.findAvailable(LocalDate.now().plusDays(1).toString(), "10:00", "11:00",
				5, null, null);

		assertEquals(1, result.size());
		assertEquals(10, result.get(0).capacity());
	}

	/** Scenario: filter by room type enum. */
	@Test
	void findAvailable_shouldFilterByType() {
		Room individual = new Room();
		individual.setId(1L);
		individual.setStatus(RoomStatus.ACTIVE);
		individual.setType(RoomType.INDIVIDUAL);

		Room group = new Room();
		group.setId(2L);
		group.setStatus(RoomStatus.ACTIVE);
		group.setType(RoomType.GROUP);

		when(roomRepository.findAll()).thenReturn(List.of(individual, group));
		when(bookingRepository.existsOverlap(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
				.thenReturn(false);

		List<RoomResponse> result = roomService.findAvailable(LocalDate.now().plusDays(1).toString(), "10:00", "11:00",
				null, "INDIVIDUAL", null);

		assertEquals(1, result.size());
		assertEquals("INDIVIDUAL", result.get(0).type());
	}

	/** Scenario: room with overlapping booking is excluded. */
	@Test
	void findAvailable_shouldExcludeRoomsWithOverlap() {
		Room room = new Room();
		room.setId(1L);
		room.setStatus(RoomStatus.ACTIVE);

		when(roomRepository.findAll()).thenReturn(List.of(room));
		when(bookingRepository.existsOverlap(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
				.thenReturn(true);

		List<RoomResponse> result = roomService.findAvailable(LocalDate.now().plusDays(1).toString(), "10:00", "11:00",
				null, null, null);

		assertEquals(0, result.size());
	}
}
