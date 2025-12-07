package com.ecoshop.dto.ContenidoEducativo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para contenido educativo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoEducativoResponseDTO {
    
    /**
     * Identificador único del contenido educativo.
     */
    private Integer contenidoId;
    
    /**
     * Título del contenido educativo.
     */
    private String titulo;
    
    /**
     * Descripción breve del contenido.
     */
    private String descripcion;
    
    /**
     * Contenido completo del artículo.
     */
    private String contenido;
    
    /**
     * Categoría del contenido educativo.
     */
    private String categoria;
    
    /**
     * URL de la imagen principal del contenido.
     */
    private String imagenUrl;
    
    /**
     * Autor del contenido educativo.
     */
    private String autor;
    
    /**
     * Fecha de creación del contenido.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última actualización del contenido.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaActualizacion;
    
    /**
     * Indica si el contenido está activo y visible.
     */
    private Boolean activo;
}

