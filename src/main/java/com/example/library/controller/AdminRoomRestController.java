package com.example.library.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.CreateRoomRequest;
import com.example.library.model.Room;
import com.example.library.repo.RoomRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomRestController {

	private final RoomRepository roomRepository;

	public AdminRoomRestController(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Room createRoom(@Valid @RequestBody CreateRoomRequest request) {

		Room room = new Room();
		room.setBuilding(request.getBuilding());
		room.setFloor(request.getFloor());
		room.setRoomNumber(request.getRoomNumber());
		room.setCapacity(request.getCapacity());
		room.setType(request.getType());
		room.setProjector(request.isProjector());
		room.setWhiteboard(request.isWhiteboard());
		room.setStatus(request.getStatus());

		return roomRepository.save(room);
	}
}