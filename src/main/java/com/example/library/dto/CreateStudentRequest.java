package com.example.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateStudentRequest {

	// Unique username for login
	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	private String username;

	// Student's full name
	@NotBlank(message = "Full name is required")
	@Size(min = 3, max = 120, message = "Full name must be between 3 and 120 characters")
	private String fullName;

	// University email address
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	// Temporary password set by admin
	@NotBlank(message = "Temporary password is required")
	@Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
	private String tempPassword;
}