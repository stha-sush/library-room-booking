package com.example.library.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.library.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

	@Query("""
			    select distinct r.building
			    from Room r
			    where r.building is not null
			      and r.building <> ''
			    order by r.building
			""")
	List<String> findDistinctBuildings();
}