package org.example.thuctap.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandlerV2 {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerV2.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        logger.warn("Resource not found: {}, path={}", ex.getMessage(), req.getRequestURI());
        ErrorResponse err = new ErrorResponse(false, ex.getMessage(), LocalDateTime.now(), req.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        logger.warn("Bad request: {}, path={}", ex.getMessage(), req.getRequestURI());
        ErrorResponse err = new ErrorResponse(false, ex.getMessage(), LocalDateTime.now(), req.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        logger.warn("Unauthorized: {}, path={}", ex.getMessage(), req.getRequestURI());
        ErrorResponse err = new ErrorResponse(false, ex.getMessage(), LocalDateTime.now(), req.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        logger.warn("Validation failed: {} errors for path={}", fieldErrors.size(), req.getRequestURI());
        ErrorResponse err = new ErrorResponse(false, "Validation error", LocalDateTime.now(), req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        logger.error("Unhandled exception on path=" + req.getRequestURI(), ex);
        ErrorResponse err = new ErrorResponse(false, "Internal server error", LocalDateTime.now(), req.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
