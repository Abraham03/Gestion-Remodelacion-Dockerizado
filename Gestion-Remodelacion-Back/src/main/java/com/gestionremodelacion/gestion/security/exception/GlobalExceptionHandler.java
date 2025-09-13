// Ubicación: com/gestionremodelacion/gestion/security/exception/GlobalExceptionHandler.java
package com.gestionremodelacion.gestion.security.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException; // 👈 1. Importamos nuestro nuevo catálogo de errores

@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Manejador consolidado para excepciones de negocio personalizadas.
         * En lugar de tener un método para cada excepción (ResourceNotFound,
         * DuplicateResource, etc.),
         * este método las captura todas, determina el código de estado HTTP correcto y
         * devuelve
         * el mensaje de la excepción, que AHORA DEBE SER UNA CLAVE del ErrorCatalog.
         */
        @ExceptionHandler({ ResourceNotFoundException.class, DuplicateResourceException.class,
                        BusinessRuleException.class })
        public ResponseEntity<ApiResponse<?>> handleCustomBusinessExceptions(RuntimeException ex, WebRequest request) {
                // Determinamos el estado HTTP basado en el tipo de excepción
                HttpStatus status = (ex instanceof ResourceNotFoundException) ? HttpStatus.NOT_FOUND
                                : HttpStatus.CONFLICT;

                log.warn("Excepción de negocio controlada: {}. Petición: {}", ex.getMessage(),
                                request.getDescription(false));

                // El mensaje de la excepción (ex.getMessage()) ya no es texto en español,
                // sino la clave que le pasamos desde el servicio (ej.
                // "error.resource.notFound").
                ApiResponse<Object> errorResponse = new ApiResponse<>(status.value(), ex.getMessage(), null);

                return new ResponseEntity<>(errorResponse, status);
        }

        /**
         * Manejador específico para violaciones de integridad de datos (ej. borrar un
         * registro con dependencias).
         * Este es el que causaba el problema original.
         */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                        WebRequest request) {

                // 👈 2. Definimos una clave de error por defecto del catálogo.
                String errorKey = ErrorCatalog.CLIENT_HAS_DEPENDENCIES.getKey();

                // 👈 3. Si la causa del error es más específica, podemos cambiar la clave.
                // Esto mantiene la lógica de negocio, pero usando claves en lugar de texto.
                if (ex.getCause() != null && ex.getCause().getMessage().contains("horas_trabajadas")) {
                        errorKey = ErrorCatalog.PROJECT_HAS_WORK_HOURS.getKey();
                }

                log.warn("Violación de integridad de datos. Petición: {}. Causa: {}", request.getDescription(false),
                                ex.getMessage());

                // 👈 4. Creamos la respuesta usando la CLAVE DE TRADUCCIÓN seleccionada.
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(), errorKey, null);

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        /**
         * Manejador para errores de validación de @Valid en los DTOs.
         * Este puede permanecer igual, ya que los mensajes de validación a menudo son
         * generados por las
         * anotaciones (ej. @NotBlank) y son específicos del campo.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                log.warn("Error de validación: {}", errors);
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                                "Error de validación: " + errors, null); // Opcionalmente, usar
                                                                         // ErrorCatalog.VALIDATION_ERROR.getKey()
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Manejador global para cualquier otra excepción no controlada.
         * Captura todo lo demás para evitar que el servidor falle con un error 500
         * genérico.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
                log.error("Ha ocurrido una excepción no controlada en la petición: {}", request.getDescription(false),
                                ex);

                // 👈 5. Usamos la clave genérica para errores inesperados de nuestro catálogo.
                ApiResponse<Object> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                ErrorCatalog.UNEXPECTED_ERROR.getKey(), null);

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}