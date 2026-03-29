package com.example.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.library.dto.CreateStudentRequest;
import com.example.library.repo.RoomRepository;
import com.example.library.repo.UserRepository;

@Controller
public class AdminPageController {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;

	public AdminPageController(UserRepository userRepository, RoomRepository roomRepository) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
	}

	@GetMapping("/admin")
	public String dashboard() {
		return "admin/dashboard";
	}

	@GetMapping("/admin/students")
	public String students(Model model) {
		model.addAttribute("students", userRepository.findAll());
		model.addAttribute("form", new CreateStudentRequest());
		return "admin/students";
	}

	@GetMapping("/admin/rooms")
	public String rooms(Model model) {
		model.addAttribute("rooms", roomRepository.findAll());
		return "admin/rooms";
	}
}