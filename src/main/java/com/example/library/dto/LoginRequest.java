package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

	// Username used for authentication
	@NotBlank(message = "Username is required")
	private String username;

	// Raw password entered by user
	@NotBlank(message = "Password is required")
	private String password;
}