package com.rsmnarts.valoo.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.rsmnarts.valoo.presentation.dto.ErrorDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorDto> handleNoHandlerFoundException(NoHandlerFoundException ex,
			HttpServletRequest request) {
		ErrorDto errorResponse = ErrorDto.builder()
				.message("The requested resource was not found.")
				.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {
		ErrorDto errorResponse = ErrorDto.builder()
				.message(ex.getMessage())
				.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorDto> handleResponseStatusException(ResponseStatusException ex,
			HttpServletRequest request) {
		ErrorDto errorResponse = ErrorDto.builder()
				.message(ex.getReason())
				.build();
		return new ResponseEntity<>(errorResponse, ex.getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> handleGlobalException(Exception ex, HttpServletRequest request) {
		ErrorDto errorResponse = ErrorDto.builder()
				.message(ex.getMessage())
				.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
