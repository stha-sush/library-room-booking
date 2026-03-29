package com.example.library.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.library.dto.RoomResponse;
import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;
import com.example.library.repo.BookingRepository;
import com.example.library.repo.RoomRepository;

@Service
public class RoomService {

	private final RoomRepository roomRepository;
	private final BookingRepository bookingRepository;

	public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
		this.roomRepository = roomRepository;
		this.bookingRepository = bookingRepository;
	}

	public List<RoomResponse> findAvailable(String dateStr, String startStr, String endStr, Integer capacity,
			String type, String building) {

		LocalDate date = LocalDate.parse(dateStr);
		LocalTime start = LocalTime.parse(startStr);
		LocalTime end = LocalTime.parse(endStr);

		List<Room> rooms = roomRepository.findAll();

		return rooms.stream()

				// Only ACTIVE rooms
				.filter(r -> r.getStatus() == RoomStatus.ACTIVE)

				// Building filter
				.filter(r -> building == null || building.isBlank() || r.getBuilding().equalsIgnoreCase(building))

				// Type filter
				.filter(r -> type == null || type.isBlank() || r.getType() == RoomType.valueOf(type))

				// Capacity filter
				.filter(r -> capacity == null || r.getCapacity() >= capacity)

				// Exclude overlapping bookings
				.filter(r -> !bookingRepository.existsOverlap(r.getId(), date, start, end))

				.map(RoomResponse::from).toList();
	}
}