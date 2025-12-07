package com.ecoshop.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso
 * para el cual no tiene permisos.
 * 
 * Esta excepción se usa para validaciones de autorización:
 * - Usuario intenta acceder a datos de otro usuario
 * - Marca intenta editar productos de otra marca
 * - Usuario sin rol adecuado intenta realizar una operación
 * 
 * Ejemplos:
 * - Usuario con ID 1 intenta actualizar usuario con ID 2
 * - Marca A intenta eliminar un producto de Marca B
 * - Cliente intenta crear una marca (solo marcas pueden crear marcas)
 * 
 * Respuesta HTTP: 403 Forbidden
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

