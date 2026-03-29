package com.example.library.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.CreateStudentRequest;
import com.example.library.dto.StudentResponse;
import com.example.library.model.UserAccount;
import com.example.library.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentRestController {

	private final UserService userService;

	public AdminStudentRestController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public StudentResponse create(@Valid @RequestBody CreateStudentRequest req) {
		UserAccount u = userService.createStudent(req);
		return new StudentResponse(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole().name());
	}
}
