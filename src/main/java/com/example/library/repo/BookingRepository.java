package com.example.library.repo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.library.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByStudentIdOrderByBookingDateDescStartTimeDesc(Long studentId);

	@Query("""
			    select (count(b) > 0)
			    from Booking b
			    where b.room.id = :roomId
			      and b.bookingDate = :bookingDate
			      and b.status = com.example.library.model.BookingStatus.CONFIRMED
			      and b.startTime < :endTime
			      and b.endTime > :startTime
			""")
	boolean existsOverlap(@Param("roomId") Long roomId, @Param("bookingDate") LocalDate bookingDate,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}