package com.vacation.Vacation_Planner_Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    // Handle business logic errors (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = switch (ex.getMessage()) {
            case "Email already in use" -> HttpStatus.CONFLICT;
            case "User not found" -> HttpStatus.NOT_FOUND;
            case "Team not found" -> HttpStatus.NOT_FOUND;
            case "Invalid invite code" -> HttpStatus.BAD_REQUEST;
            case "Already a member of this team" -> HttpStatus.CONFLICT;
            case "You already have a team" -> HttpStatus.CONFLICT;
            case "Vacation request not found" -> HttpStatus.NOT_FOUND;
            case "Not enough vacation days" -> HttpStatus.BAD_REQUEST;
            case "Only pending vacation requests can be cancelled" -> HttpStatus.BAD_REQUEST;
            case "Notification not found" -> HttpStatus.NOT_FOUND;
            case "Vacation balance not found" -> HttpStatus.NOT_FOUND;
            case "User is not in a team" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return buildResponse(status, ex.getMessage(), null);
    }

    // Handle 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", null);
    }

    // Handle 401 Unauthorized
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", null);
    }

    // Build response body
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        if (details != null) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}
