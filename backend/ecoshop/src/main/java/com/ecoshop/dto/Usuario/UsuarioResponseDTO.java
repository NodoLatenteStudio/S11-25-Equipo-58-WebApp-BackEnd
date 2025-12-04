package com.ecoshop.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de usuarios.
 * 
 * Esta clase representa los datos de un usuario que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs de respuesta?
 * - Control sobre qué datos se exponen en la API
 * - Evita exponer información sensible (password_hash nunca se expone)
 * - Permite transformar datos antes de enviarlos al cliente
 * - Facilita versionado de la API
 * 
 * Campos:
 * - usuarioId: Identificador único del usuario
 * - clerkId: ID del usuario en Clerk (si está integrado)
 * - email: Email del usuario
 * - nombre: Nombre del usuario
 * - direccionDefault: Dirección por defecto del usuario
 * - rol: Rol del usuario en el sistema
 * - fechaRegistro: Fecha de registro del usuario
 * 
 * Nota: El password_hash nunca se incluye en este DTO por seguridad.
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class UsuarioResponseDTO {

    /**
     * Identificador único del usuario.
     * 
     * Este ID es generado automáticamente por la base de datos
     * cuando se crea el usuario.
     */
    private Integer usuarioId;

    /**
     * ID del usuario en Clerk.
     * 
     * Este campo vincula el usuario local con el usuario en Clerk.
     * Puede ser null si el usuario no está integrado con Clerk.
     */
    private String clerkId;

    /**
     * Email del usuario.
     * 
     * Email único que identifica al usuario en el sistema.
     */
    private String email;

    /**
     * Nombre del usuario.
     * 
     * Nombre completo o nombre de display del usuario.
     */
    private String nombre;

    /**
     * Dirección por defecto del usuario.
     * 
     * Dirección de envío por defecto que se usa para prellenar formularios de pedidos.
     */
    private String direccionDefault;

    /**
     * Rol del usuario en el sistema.
     * 
     * Valores posibles:
     * - "cliente": Usuario regular que puede realizar pedidos
     * - "marca": Usuario que representa una marca y puede gestionar productos
     * - "admin": Administrador del sistema con acceso completo
     */
    private String rol;

    /**
     * Fecha de registro del usuario.
     * 
     * Fecha en que el usuario se registró en el sistema.
     * Esta fecha se establece automáticamente y no se puede modificar.
     */
    private LocalDateTime fechaRegistro;
}
