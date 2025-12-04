package com.ecoshop.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones.
 * 
 * Esta clase captura y maneja todas las excepciones que se lanzan en los controladores,
 * proporcionando respuestas HTTP consistentes y estructuradas.
 * 
 * ¿Por qué usar un manejador global de excepciones?
 * - Centraliza el manejo de errores en un solo lugar
 * - Proporciona respuestas HTTP consistentes
 * - Evita código duplicado en los controladores
 * - Facilita el mantenimiento y la depuración
 * - Mejora la experiencia del usuario con mensajes de error claros
 * 
 * Excepciones manejadas:
 * 1. EntityNotFoundException: Cuando no se encuentra un recurso (404)
 * 2. BadRequestException: Cuando se viola una regla de negocio (400)
 * 3. MethodArgumentNotValidException: Cuando falla la validación de datos (400)
 * 4. Exception: Cualquier otra excepción no manejada (500)
 * 
 * @RestControllerAdvice: Esta anotación hace que esta clase capture excepciones
 * de todos los controladores REST de la aplicación.
 */
@RestControllerAdvice // Captura excepciones de todos los controladores REST
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones cuando no se encuentra una entidad.
     * 
     * Esta excepción se lanza cuando se intenta acceder a un recurso que no existe
     * en la base de datos (ej: producto con ID que no existe).
     * 
     * Ejemplo de uso:
     * - GET /api/v1/productos/999 (producto con ID 999 no existe)
     * - DELETE /api/v1/productos/999 (producto con ID 999 no existe)
     * 
     * Respuesta HTTP:
     * - Código: 404 (Not Found)
     * - Body: { "timestamp": "...", "status": 404, "error": "Not Found", "message": "..." }
     * 
     * @param ex Excepción EntityNotFoundException
     * @return ResponseEntity con el error y código HTTP 404
     */
    @ExceptionHandler(EntityNotFoundException.class) // Maneja excepciones de tipo EntityNotFoundException
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        // Construye la respuesta de error con la información de la excepción
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now()) // Hora actual del error
                .status(HttpStatus.NOT_FOUND.value()) // Código HTTP 404
                .error("Not Found") // Tipo de error
                .message(ex.getMessage()) // Mensaje de la excepción (ej: "Producto no encontrado con id: 999")
                .build();
        // Retorna la respuesta con código HTTP 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones cuando se produce un error de validación de negocio.
     * 
     * Esta excepción se lanza cuando se viola una regla de negocio o se intenta
     * realizar una operación con datos inválidos (ej: certificación no encontrada).
     * 
     * Ejemplo de uso:
     * - POST /api/v1/productos con código de certificación que no existe
     * - PUT /api/v1/productos/1 con código de certificación inválido
     * 
     * Respuesta HTTP:
     * - Código: 400 (Bad Request)
     * - Body: { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }
     * 
     * @param ex Excepción BadRequestException
     * @return ResponseEntity con el error y código HTTP 400
     */
    @ExceptionHandler(BadRequestException.class) // Maneja excepciones de validación de negocio
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        // Construye la respuesta de error con la información de la excepción
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now()) // Hora actual del error
                .status(HttpStatus.BAD_REQUEST.value()) // Código HTTP 400
                .error("Bad Request") // Tipo de error
                .message(ex.getMessage()) // Mensaje de la excepción
                .build();
        // Retorna la respuesta con código HTTP 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones cuando falla la validación de datos.
     * 
     * Esta excepción se lanza cuando los datos enviados en una petición no cumplen
     * con las validaciones definidas en los DTOs (ej: nombre vacío, precio negativo).
     * 
     * Ejemplo de uso:
     * - POST /api/v1/productos con nombre vacío
     * - PUT /api/v1/productos/1 con precio negativo
     * 
     * Respuesta HTTP:
     * - Código: 400 (Bad Request)
     * - Body: {
     *     "timestamp": "...",
     *     "status": 400,
     *     "error": "Validation Failed",
     *     "message": "Errores de validación en los campos",
     *     "errors": {
     *       "nombre": "El nombre del producto es obligatorio",
     *       "precio": "El precio debe ser mayor a 0"
     *     }
     *   }
     * 
     * @param ex Excepción MethodArgumentNotValidException
     * @return ResponseEntity con los errores de validación y código HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // Maneja excepciones de validación
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Map para almacenar los errores de validación por campo
        Map<String, String> errors = new HashMap<>();
        
        // Itera sobre todos los errores de validación
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Obtiene el nombre del campo que falló la validación
            String fieldName = ((FieldError) error).getField();
            // Obtiene el mensaje de error definido en la anotación de validación
            String errorMessage = error.getDefaultMessage();
            // Almacena el error en el map
            errors.put(fieldName, errorMessage);
        });

        // Construye la respuesta con los errores de validación
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now()); // Hora actual del error
        response.put("status", HttpStatus.BAD_REQUEST.value()); // Código HTTP 400
        response.put("error", "Validation Failed"); // Tipo de error
        response.put("message", "Errores de validación en los campos"); // Mensaje general
        response.put("errors", errors); // Map con los errores por campo
        
        // Retorna la respuesta con código HTTP 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja cualquier otra excepción no manejada específicamente.
     * 
     * Este método captura todas las excepciones que no fueron manejadas por
     * los otros métodos (EntityNotFoundException, MethodArgumentNotValidException).
     * 
     * Ejemplo de uso:
     * - Errores de base de datos
     * - Errores de red
     * - Errores inesperados en el código
     * 
     * Respuesta HTTP:
     * - Código: 500 (Internal Server Error)
     * - Body: {
     *     "timestamp": "...",
     *     "status": 500,
     *     "error": "Internal Server Error",
     *     "message": "Ha ocurrido un error inesperado"
     *   }
     * 
     * NOTA: En producción, no expondremos los detalles de los errores por seguridad.
     * 
     * @param ex Excepción genérica
     * @return ResponseEntity con el error y código HTTP 500
     */
    @ExceptionHandler(Exception.class) // Maneja cualquier otra excepción
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Construye la respuesta de error genérica
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now()) // Hora actual del error
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Código HTTP 500
                .error("Internal Server Error") // Tipo de error
                // No exponemos el mensaje de la excepción por seguridad
                // En producción, registrar el error en un log y mostrar un mensaje genérico
                .message("Ha ocurrido un error inesperado") // Mensaje genérico
                .build();
        // Retorna la respuesta con código HTTP 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Clase interna que representa la respuesta de error.
     * 
     * Esta clase se usa para estructurar las respuestas de error de forma consistente.
     * 
     * Campos:
     * - timestamp: Hora en que ocurrió el error
     * - status: Código HTTP del error (404, 400, 500, etc.)
     * - error: Tipo de error (Not Found, Validation Failed, Internal Server Error)
     * - message: Mensaje descriptivo del error
     */
    @lombok.Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
    @lombok.Builder // Permite construir objetos usando el patrón Builder (Lombok)
    @lombok.NoArgsConstructor // Genera constructor sin argumentos
    @lombok.AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
    public static class ErrorResponse {
        private LocalDateTime timestamp; // Hora en que ocurrió el error
        private int status; // Código HTTP del error
        private String error; // Tipo de error
        private String message; // Mensaje descriptivo del error
    }
}

