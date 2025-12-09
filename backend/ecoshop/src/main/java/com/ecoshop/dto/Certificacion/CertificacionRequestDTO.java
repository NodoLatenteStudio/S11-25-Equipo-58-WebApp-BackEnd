package com.ecoshop.dto.Certificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear y actualizar certificaciones.
 * 
 * Esta clase representa los datos de una certificación que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotBlank, @Size, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos según el esquema de la base de datos:
 * - nombreSello: Nombre del sello de certificación (obligatorio)
 * - descripcion: Descripción de la certificación (opcional)
 * - entidadEmisora: Entidad que emite la certificación (opcional)
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class CertificacionRequestDTO {

    /**
     * Nombre del sello de certificación.
     * 
     * Campo opcional en actualizaciones (PUT), pero obligatorio en creaciones (POST).
     * La validación se realiza manualmente en el servicio para permitir actualizaciones parciales y facilitar el uso en el frontend.
     * 
     * Este es el nombre que se mostrará a los usuarios en el frontend.
     * 
     * Ejemplos válidos:
     * - "Fair Trade"
     * - "Carbon Neutral"
     * - "Organic"
     * - "Comercio Justo"
     * 
     * Nota: En POST es obligatorio, en PUT es opcional para permitir actualizaciones parciales.
     */
    private String nombreSello;

    /**
     * Descripción de la certificación.
     * 
     * Campo opcional que permite proporcionar información detallada sobre la certificación.
     * 
     * Ejemplos:
     * - "Certificación que garantiza condiciones de comercio justo para productores"
     * - "Certificación que verifica la neutralidad de carbono del producto"
     */
    private String descripcion;

    /**
     * Entidad que emite la certificación.
     * 
     * Campo opcional que indica qué organización o entidad es responsable
     * de emitir y verificar esta certificación.
     * 
     * Ejemplos:
     * - "Fair Trade International"
     * - "Carbon Trust"
     * - "USDA Organic"
     * - "Organización Internacional de Certificación"
     */
    private String entidadEmisora;

    /**
     * URL de la imagen del sello de certificación.
     * 
     * Campo opcional que almacena la dirección de la imagen del sello de certificación.
     * Útil para mostrar el sello en el frontend.
     * 
     * Ejemplos: "https://i.ibb.co/xxxxx/carbon-neutral.png", "https://cdn.example.com/certificaciones/b-corp.png"
     */
    private String imagenUrl;
}

