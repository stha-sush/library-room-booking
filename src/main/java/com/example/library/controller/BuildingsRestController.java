package com.example.library.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.repo.RoomRepository;

@RestController
public class BuildingsRestController {

	private final RoomRepository roomRepository;

	public BuildingsRestController(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	@GetMapping("/api/buildings")
	public List<String> getBuildings() {
		return roomRepository.findDistinctBuildings();
	}
}