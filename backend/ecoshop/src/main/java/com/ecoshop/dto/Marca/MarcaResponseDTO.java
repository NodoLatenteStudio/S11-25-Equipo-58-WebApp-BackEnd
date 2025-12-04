package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de marcas.
 * 
 * Esta clase representa los datos de una marca que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs de respuesta?
 * - Control sobre qué datos se exponen en la API
 * - Evita exponer información sensible o innecesaria
 * - Permite transformar datos antes de enviarlos al cliente
 * - Facilita versionado de la API
 * 
 * Campos:
 * - marcaId: Identificador único de la marca
 * - usuarioId: ID del usuario propietario de la marca
 * - nombreOficial: Nombre oficial de la marca
 * - descripcionSostenible: Descripción de prácticas sostenibles
 * - sitioWeb: URL del sitio web de la marca
 * - logoUrl: URL del logo de la marca
 * - fechaUnion: Fecha en que la marca se unió a la plataforma
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class MarcaResponseDTO {

    /**
     * Identificador único de la marca.
     * 
     * Este ID es generado automáticamente por la base de datos
     * cuando se crea la marca.
     */
    private Integer marcaId;

    /**
     * ID del usuario propietario de la marca.
     * 
     * Referencia al usuario que creó y gestiona esta marca.
     */
    private Integer usuarioId;

    /**
     * Nombre oficial de la marca.
     * 
     * Ejemplos: "EcoLife", "GreenTech Solutions", "Sustentable S.A."
     */
    private String nombreOficial;

    /**
     * Descripción de las prácticas sostenibles de la marca.
     * 
     * Información sobre las prácticas sostenibles, certificaciones
     * y compromisos ambientales de la marca.
     */
    private String descripcionSostenible;

    /**
     * URL del sitio web de la marca.
     * 
     * Dirección del sitio web oficial de la marca.
     * Ejemplos: "https://www.ecolife.com", "https://greentech.com"
     */
    private String sitioWeb;

    /**
     * URL del logo de la marca.
     * 
     * Dirección de la imagen del logo de la marca.
     * Útil para mostrar el logo en el frontend.
     */
    private String logoUrl;

    /**
     * Fecha en que la marca se unió a la plataforma.
     * 
     * Esta fecha se establece automáticamente cuando se crea la marca
     * y no se puede modificar posteriormente.
     */
    private LocalDateTime fechaUnion;
}
