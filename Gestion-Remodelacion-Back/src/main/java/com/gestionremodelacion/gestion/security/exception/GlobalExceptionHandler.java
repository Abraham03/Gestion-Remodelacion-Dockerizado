package com.gestionremodelacion.gestion.security.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
            WebRequest request) {

        // Mensaje por defecto por si no podemos identificar la causa exacta
        String userFriendlyMessage = "No se puede eliminar el registro porque tiene datos asociados en otras partes del sistema.";

        ApiResponse<Object> errorResponse = new ApiResponse<>(
                HttpStatus.CONFLICT.value(),
                userFriendlyMessage,
                null);

        // Devolvemos una respuesta 409 Conflict bien formada
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
