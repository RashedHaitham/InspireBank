package com.example.employeeService.exception;

import jakarta.validation.ValidationException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeServiceExceptionHandlerTest {

    private EmployeeServiceExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new EmployeeServiceExceptionHandler();
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        EmployeeServiceExceptionHandler.ErrorResponse body = (EmployeeServiceExceptionHandler.ErrorResponse) response.getBody();
        assertEquals("Resource not found", body.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), body.getStatusCode());
    }

    @Test
    void testHandleGlobalException() {
        // Arrange
        Exception ex = new Exception("Internal server error");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGlobalException(ex, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        EmployeeServiceExceptionHandler.ErrorResponse body = (EmployeeServiceExceptionHandler.ErrorResponse) response.getBody();
        assertEquals("An unexpected error occurred: Internal server error", body.getMessage()); // Updated message format
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getStatusCode());
    }



    @Test
    void testHandleValidationExceptions() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("objectName", "field", "error message");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("field", "error message");
        assertEquals(expectedErrors, response.getBody());
    }

    @Test
    void testHandleValidationException() {
        // Arrange
        ValidationException ex = new ValidationException("Validation failed");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGlobalException(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        EmployeeServiceExceptionHandler.ErrorResponse body = (EmployeeServiceExceptionHandler.ErrorResponse) response.getBody();
        assertEquals("Validation failed", body.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatusCode());
    }
}
