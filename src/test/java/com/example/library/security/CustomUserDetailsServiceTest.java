package com.example.library.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	/** Scenario: user exists; authorities include ROLE_* from account. */
	@Test
	void loadUserByUsername_found_returnsAuthorities() {
		UserAccount u = new UserAccount();
		u.setUsername("student1");
		u.setPasswordHash("hash");
		u.setRole(UserRole.STUDENT);

		when(userRepository.findByUsername("student1")).thenReturn(Optional.of(u));

		UserDetails details = customUserDetailsService.loadUserByUsername("student1");

		assertEquals("student1", details.getUsername());
		assertEquals("hash", details.getPassword());
		assertTrue(details.getAuthorities().stream().anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority())));
	}

	/** Scenario: username not in database. */
	@Test
	void loadUserByUsername_missing_throwsUsernameNotFoundException() {
		when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("nobody"));
	}
}
