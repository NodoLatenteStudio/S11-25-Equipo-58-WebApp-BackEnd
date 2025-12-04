package com.ecoshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para respuestas de certificaciones.
 * 
 * Esta clase representa los datos de una certificación que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se exponen en la API
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * - Mejor rendimiento al evitar cargar relaciones innecesarias
 * 
 * Diferencias con CertificacionRequestDTO:
 * - Incluye campos calculados o derivados (certificacionId)
 * - Incluye campos de solo lectura que se asignan automáticamente
 * - No incluye relaciones (productos) para evitar referencias circulares
 * 
 * Campos según el esquema de la base de datos:
 * - certificacionId: Identificador único de la certificación (generado por la BD)
 * - nombreSello: Nombre del sello de certificación
 * - descripcion: Descripción de la certificación
 * - entidadEmisora: Entidad que emite la certificación
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class CertificacionResponseDTO {

    /**
     * Identificador único de la certificación.
     * 
     * Este campo se asigna automáticamente por la base de datos cuando se crea la certificación.
     * Se incluye en todas las respuestas para que el cliente pueda identificar la certificación.
     * 
     * Se usa principalmente para operaciones de actualización y eliminación.
     */
    private Integer certificacionId;

    /**
     * Nombre del sello de certificación.
     * 
     * Este es el nombre que se muestra a los usuarios en el frontend.
     * 
     * Ejemplos: "Fair Trade", "Carbon Neutral", "Organic", "Comercio Justo"
     */
    private String nombreSello;

    /**
     * Descripción de la certificación.
     * 
     * Proporciona información detallada sobre la certificación.
     * 
     * Ejemplos: "Certificación que garantiza condiciones de comercio justo para productores"
     */
    private String descripcion;

    /**
     * Entidad que emite la certificación.
     * 
     * Indica qué organización o entidad es responsable de emitir y verificar esta certificación.
     * 
     * Ejemplos: "Fair Trade International", "Carbon Trust", "USDA Organic"
     */
    private String entidadEmisora;
}

