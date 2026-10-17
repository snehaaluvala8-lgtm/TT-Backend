package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.ApiError;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
			.collect(Collectors.toMap(FieldError::getField, error -> error.getDefaultMessage() == null
				? "Invalid value."
				: error.getDefaultMessage(), (first, ignored) -> first));
		return error(HttpStatus.BAD_REQUEST, "Please check the submitted fields.", validationErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return error(HttpStatus.BAD_REQUEST, "The request body is missing or malformed.", Map.of());
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleStatus(ResponseStatusException exception) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
		return error(status, message, Map.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataConflict(DataIntegrityViolationException exception) {
		return error(HttpStatus.CONFLICT, "The request conflicts with existing data.", Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
		logger.error("Unexpected API error.", exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.", Map.of());
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> validationErrors) {
		ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
			validationErrors);
		return ResponseEntity.status(status).body(body);
	}
}
