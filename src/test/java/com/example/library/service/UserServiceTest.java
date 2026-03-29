package com.example.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.library.dto.CreateStudentRequest;
import com.example.library.model.UserRole;
import com.example.library.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	/** Scenario: admin creates a new student; password is hashed and role is STUDENT. */
	@Test
	void createStudent_succeeds_encodesPassword_andSetsStudentRole() {
		CreateStudentRequest req = new CreateStudentRequest();
		req.setUsername("newstudent");
		req.setFullName("New Student");
		req.setEmail("new@univ.edu");
		req.setTempPassword("TempPass12");

		when(userRepository.existsByUsername("newstudent")).thenReturn(false);
		when(userRepository.existsByEmail("new@univ.edu")).thenReturn(false);
		when(passwordEncoder.encode(eq("TempPass12"))).thenReturn("HASHED");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		var saved = userService.createStudent(req);

		assertEquals("newstudent", saved.getUsername());
		assertEquals(UserRole.STUDENT, saved.getRole());
		assertEquals("HASHED", saved.getPasswordHash());

		ArgumentCaptor<String> rawPass = ArgumentCaptor.forClass(String.class);
		verify(passwordEncoder).encode(rawPass.capture());
		assertEquals("TempPass12", rawPass.getValue());
	}

	/** Scenario: username already exists. */
	@Test
	void createStudent_duplicateUsername_throwsIllegalArgumentException() {
		CreateStudentRequest req = new CreateStudentRequest();
		req.setUsername("dup");
		req.setFullName("Dup User");
		req.setEmail("dup@univ.edu");
		req.setTempPassword("TempPass12");

		when(userRepository.existsByUsername("dup")).thenReturn(true);

		var ex = assertThrows(IllegalArgumentException.class, () -> userService.createStudent(req));
		assertEquals("Username already exists", ex.getMessage());
	}

	/** Scenario: email already exists. */
	@Test
	void createStudent_duplicateEmail_throwsIllegalArgumentException() {
		CreateStudentRequest req = new CreateStudentRequest();
		req.setUsername("user1");
		req.setFullName("User One");
		req.setEmail("same@univ.edu");
		req.setTempPassword("TempPass12");

		when(userRepository.existsByUsername("user1")).thenReturn(false);
		when(userRepository.existsByEmail("same@univ.edu")).thenReturn(true);

		var ex = assertThrows(IllegalArgumentException.class, () -> userService.createStudent(req));
		assertEquals("Email already exists", ex.getMessage());
	}
}
