package com.example.library.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.library.model.RoomType;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("title", "Home");
		model.addAttribute("today", LocalDate.now().toString());
		model.addAttribute("types", RoomType.values());
		return "index";
	}
}