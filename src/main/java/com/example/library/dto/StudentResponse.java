package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

	// Student ID
	private Long id;

	// Username used for login
	private String username;

	// Student's full name
	private String fullName;

	// Student email address
	private String email;

	// Role (ADMIN or STUDENT)
	private String role;
}