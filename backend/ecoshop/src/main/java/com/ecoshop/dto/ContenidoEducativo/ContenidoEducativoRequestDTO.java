package com.ecoshop.dto.ContenidoEducativo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para crear o actualizar contenido educativo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoEducativoRequestDTO {
    
    /**
     * Título del contenido educativo.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;
    
    /**
     * Descripción breve del contenido.
     */
    private String descripcion;
    
    /**
     * Contenido completo del artículo.
     */
    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;
    
    /**
     * Categoría del contenido educativo.
     */
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria;
    
    /**
     * URL de la imagen principal del contenido.
     */
    private String imagenUrl;
    
    /**
     * Autor del contenido educativo.
     */
    @Size(max = 100, message = "El autor no puede exceder 100 caracteres")
    private String autor;
    
    /**
     * Indica si el contenido está activo y visible.
     */
    private Boolean activo;
}

