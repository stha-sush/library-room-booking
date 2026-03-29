package com.example.library.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.library.model.Room;
import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;

/**
 * JPA test for findDistinctBuildings ordering.
 */
@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

	@Autowired
	private RoomRepository roomRepository;

	/** Scenario: distinct building names sorted ascending. */
	@Test
	void findDistinctBuildings_sortedDistinct() {
		roomRepository.save(room("Main", "A"));
		roomRepository.save(room("Main", "B"));
		roomRepository.save(room("Science", "C"));

		List<String> buildings = roomRepository.findDistinctBuildings();

		assertEquals(List.of("Main", "Science"), buildings);
	}

	private Room room(String building, String number) {
		Room r = new Room();
		r.setBuilding(building);
		r.setFloor(1);
		r.setRoomNumber(number);
		r.setCapacity(4);
		r.setType(RoomType.GROUP);
		r.setProjector(false);
		r.setWhiteboard(true);
		r.setStatus(RoomStatus.ACTIVE);
		return r;
	}
}
