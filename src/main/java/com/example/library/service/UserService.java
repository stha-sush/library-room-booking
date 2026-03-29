package com.example.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library.dto.CreateStudentRequest;
import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.repo.UserRepository;

import jakarta.validation.Valid;

@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserAccount createStudent(@Valid CreateStudentRequest req) {
		if (userRepository.existsByUsername(req.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}
		if (userRepository.existsByEmail(req.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		UserAccount u = new UserAccount();
		u.setUsername(req.getUsername());
		u.setFullName(req.getFullName());
		u.setEmail(req.getEmail());
		u.setRole(UserRole.STUDENT);
		u.setPasswordHash(passwordEncoder.encode(req.getTempPassword()));

		UserAccount saved = userRepository.save(u);
		log.info("Created student account username={}", saved.getUsername());
		return saved;
	}
}