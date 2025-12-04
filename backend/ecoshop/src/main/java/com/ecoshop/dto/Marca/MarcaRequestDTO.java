package com.ecoshop.dto.Marca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear y actualizar marcas.
 * 
 * Esta clase representa los datos de una marca que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @NotBlank, @Size, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos:
 * - usuarioId: ID del usuario propietario de la marca (obligatorio)
 * - nombreOficial: Nombre oficial de la marca (obligatorio, máximo 150 caracteres)
 * - descripcionSostenible: Descripción de prácticas sostenibles (opcional)
 * - sitioWeb: URL del sitio web de la marca (opcional)
 * - logoUrl: URL del logo de la marca (opcional)
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class MarcaRequestDTO {

    /**
     * ID del usuario propietario de la marca.
     * 
     * NOTA: Este campo se establece automáticamente desde el usuario autenticado
     * en el controller. Si se proporciona en el body, será sobrescrito por el
     * usuarioId del token JWT para mayor seguridad.
     * 
     * El usuario debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    private Integer usuarioId;

    /**
     * Nombre oficial de la marca.
     * 
     * @NotBlank: El campo no puede estar vacío ni ser null
     * @Size: Longitud máxima de 150 caracteres
     * 
     * Ejemplos: "EcoLife", "GreenTech Solutions", "Sustentable S.A."
     */
    @NotBlank(message = "El nombre oficial es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreOficial;

    /**
     * Descripción de las prácticas sostenibles de la marca.
     * 
     * Campo opcional que permite proporcionar información sobre
     * las prácticas sostenibles, certificaciones y compromisos ambientales.
     */
    private String descripcionSostenible;

    /**
     * URL del sitio web de la marca.
     * 
     * Campo opcional que almacena la dirección del sitio web oficial.
     * Ejemplos: "https://www.ecolife.com", "https://greentech.com"
     */
    private String sitioWeb;

    /**
     * URL del logo de la marca.
     * 
     * Campo opcional que almacena la dirección de la imagen del logo.
     * Útil para mostrar el logo en el frontend.
     * Ejemplos: "https://cdn.example.com/logos/ecolife.png"
     */
    private String logoUrl;
}
