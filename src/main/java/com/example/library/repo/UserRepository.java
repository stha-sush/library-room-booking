package com.example.library.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.library.model.UserAccount;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
	Optional<UserAccount> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}