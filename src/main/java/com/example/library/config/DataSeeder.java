package com.example.library.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;
import com.example.library.model.UserAccount;
import com.example.library.model.UserRole;
import com.example.library.repo.RoomRepository;
import com.example.library.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {

		if (userRepository.count() == 0) {
			userRepository.save(createUser("admin", "Library Admin", "admin@library.edu", UserRole.ADMIN, "Admin@123"));
			userRepository
					.save(createUser("student1", "Student One", "student1@univ.edu", UserRole.STUDENT, "Student@123"));

			log.info("Seeded default users: admin/Admin@123 and student1/Student@123");
		}

		if (roomRepository.count() == 0) {
			roomRepository.saveAll(List.of(createRoom("Main", 1, "R101", 4, RoomType.GROUP, true, true),
					createRoom("Main", 2, "R201", 2, RoomType.INDIVIDUAL, false, true),
					createRoom("Science", 3, "S301", 8, RoomType.GROUP, true, false)));

			log.info("Seeded sample rooms");
		}
	}

	// Create user with BCrypt password
	private UserAccount createUser(String username, String fullName, String email, UserRole role, String rawPassword) {
		UserAccount u = new UserAccount();
		u.setUsername(username);
		u.setFullName(fullName);
		u.setEmail(email);
		u.setRole(role);
		u.setPasswordHash(passwordEncoder.encode(rawPassword));
		return u;
	}

	// Create active room
	private Room createRoom(String building, int floor, String roomNumber, int capacity, RoomType type,
			boolean projector, boolean whiteboard) {
		Room r = new Room();
		r.setBuilding(building);
		r.setFloor(floor);
		r.setRoomNumber(roomNumber);
		r.setCapacity(capacity);
		r.setType(type);
		r.setProjector(projector);
		r.setWhiteboard(whiteboard);
		r.setStatus(RoomStatus.ACTIVE);
		return r;
	}
}