package com.example.library.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.dto.RoomResponse;
import com.example.library.service.RoomService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityRestController {

	private final RoomService roomService;

	public AvailabilityRestController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	public List<RoomResponse> available(@RequestParam @NotBlank String date, @RequestParam @NotBlank String start,
			@RequestParam @NotBlank String end, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String type, @RequestParam(required = false) String building) {
		LocalDate requested = LocalDate.parse(date);
		if (requested.isBefore(LocalDate.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past dates cannot be searched.");
		}

		return roomService.findAvailable(date, start, end, capacity, type, building);
	}
}