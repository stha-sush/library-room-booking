package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	// JWT token issued after successful authentication
	private String token;

	// Username of the authenticated user
	private String username;

	// Role of the authenticated user (ADMIN or STUDENT)
	private String role;
}