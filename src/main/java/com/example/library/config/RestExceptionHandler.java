package com.example.library.config;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {

	public record ApiError(Instant timestamp, int status, String message, Map<String, String> errors, String path) {
	}

	private ApiError body(HttpStatus status, String message, Map<String, String> errors, HttpServletRequest req) {
		return new ApiError(Instant.now(), status.value(), message, errors, req.getRequestURI());
	}

	// Wrong username/password
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(body(HttpStatus.UNAUTHORIZED, "Invalid username or password.", null, req));
	}

	// Other auth failures
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(body(HttpStatus.UNAUTHORIZED, "Please log in to continue.", null, req));
	}

	// Forbidden
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(body(HttpStatus.FORBIDDEN, "Access denied.", null, req));
	}

	// Invalid date/time query params (e.g. LocalDate.parse failure)
	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<ApiError> handleDateTimeParse(DateTimeParseException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST,
				"Invalid date or time format.", null, req));
	}

	// Invalid JSON / enum mismatch (THIS is what you hit when RoomType is wrong)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST,
				"Invalid request body. Please check the entered values and try again.", null, req));
	}

	// Validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError err : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage());
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(body(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields.", fieldErrors, req));
	}

	// Your business errors
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(body(HttpStatus.BAD_REQUEST, ex.getMessage(), null, req));
	}

	// Not found
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, ex.getMessage(), null, req));
	}

	// Status exceptions
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex, HttpServletRequest req) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		String msg = ex.getReason() == null ? "Request failed." : ex.getReason();
		return ResponseEntity.status(status).body(body(status, msg, null, req));
	}

	// Fallback
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", null, req));
	}
}