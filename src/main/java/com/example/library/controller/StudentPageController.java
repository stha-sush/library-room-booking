package com.example.library.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.library.service.BookingService;

@Controller
public class StudentPageController {

	private final BookingService bookingService;

	public StudentPageController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@GetMapping("/student/my-bookings")
	public String myBookings(Model model, Principal principal) {
		model.addAttribute("title", "My Bookings");
		model.addAttribute("bookings", bookingService.getMyBookings(principal.getName()));
		return "student/my-bookings";
	}
}