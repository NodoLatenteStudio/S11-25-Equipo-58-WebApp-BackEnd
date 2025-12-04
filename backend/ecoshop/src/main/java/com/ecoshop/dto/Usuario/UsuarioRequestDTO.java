package com.ecoshop.dto.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear y actualizar usuarios.
 * 
 * Esta clase representa los datos de un usuario que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @NotBlank, @Email, @Pattern, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos:
 * - clerkId: ID del usuario en Clerk (opcional, para integración con Clerk)
 * - email: Email del usuario (obligatorio, formato válido)
 * - password: Contraseña del usuario (opcional - Clerk maneja autenticación)
 * - nombre: Nombre del usuario (opcional)
 * - direccionDefault: Dirección por defecto del usuario (opcional)
 * - rol: Rol del usuario (obligatorio, valores: cliente, marca, admin)
 * 
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class UsuarioRequestDTO {

    /**
     * ID del usuario en Clerk.
     * 
     * Campo opcional que vincula el usuario local con el usuario en Clerk.
     * Se usa cuando se integra Clerk para autenticación.
     * 
     * Con Clerk integrado:
     * - Este campo se llena automáticamente desde webhooks de Clerk
     * - Se usa para buscar usuarios cuando se valida un token JWT de Clerk
     */
    private String clerkId;

    /**
     * Email del usuario.
     * 
     * @NotBlank: El campo no puede estar vacío ni ser null
     * @Email: Debe tener un formato de email válido
     * 
     * El email debe ser único en la base de datos.
     * 
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    /**
     * Contraseña del usuario.
     * 
     * Campo opcional. Si se proporciona, se hasheará antes de guardar.
     * 
     */
    private String password;

    /**
     * Nombre del usuario.
     * 
     * Campo opcional que almacena el nombre completo o nombre de display del usuario.
     * 
     */
    private String nombre;

    /**
     * Dirección por defecto del usuario.
     * 
     * Campo opcional que almacena la dirección de envío por defecto.
     * Útil para prellenar formularios de pedidos.
     */
    private String direccionDefault;

    /**
     * Rol del usuario en el sistema.
     * 
     * @NotBlank: El campo no puede estar vacío ni ser null
     * @Pattern: Solo acepta valores específicos: "cliente", "marca", "admin"
     * 
     * Valores posibles:
     * - "cliente": Usuario regular que puede realizar pedidos
     * - "marca": Usuario que representa una marca y puede gestionar productos
     * - "admin": Administrador del sistema con acceso completo
     * 
     */
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "cliente|marca|admin", message = "El rol debe ser: cliente, marca o admin")
    private String rol;
}
