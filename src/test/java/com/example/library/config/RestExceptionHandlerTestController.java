//package com.example.library.config;
//
//import java.time.format.DateTimeParseException;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.server.ResponseStatusException;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import lombok.Data;
//
//@RestController
//@RequestMapping("/__test/errors")
//public class RestExceptionHandlerTestController {
//
//	// Triggers ResponseStatusException -> handler maps status + body.
//	@GetMapping("/status")
//	public void throwsResponseStatus() {
//		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "missing");
//	}
//
//	// Triggers MethodArgumentNotValidException when body is empty JSON.
//	@PostMapping("/valid")
//	public void validation(@Valid @RequestBody ValidationSample body) {
//		// body validated
//	}
//
//	// Triggers DateTimeParseException handler path.
//	@GetMapping("/parse")
//	public void throwsDateTimeParse() {
//		throw new DateTimeParseException("invalid", "x", 0);
//	}
//
//	@Data
//	public static class ValidationSample {
//		@NotBlank
//		private String name;
//	}
//}
