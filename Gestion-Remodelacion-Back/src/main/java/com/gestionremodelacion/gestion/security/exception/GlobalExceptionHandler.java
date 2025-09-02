package com.gestionremodelacion.gestion.security.exception;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex,
                        WebRequest request) {
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
                                null);
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<?>> handleDuplicateResourceException(DuplicateResourceException ex,
                        WebRequest request) {
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(), ex.getMessage(),
                                null);
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ApiResponse<?>> handleBusinessRuleException(BusinessRuleException ex,
                        WebRequest request) {
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(), ex.getMessage(),
                                null);
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                        WebRequest request) {
                String message = "No se puede eliminar: el registro tiene dependencias en otras partes del sistema.";
                // Lógica mejorada para dar un mensaje más específico si es posible
                if (ex.getCause() != null && ex.getCause().getMessage().contains("horas_trabajadas")) {
                        message = "No se puede eliminar el proyecto porque tiene horas trabajadas registradas.";
                }
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(), message, null);
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                                "Error de validación: " + errors, null);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Ha ocurrido un error inesperado: " + ex.getMessage(), null);
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
