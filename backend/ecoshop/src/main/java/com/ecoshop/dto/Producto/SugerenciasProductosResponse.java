package com.ecoshop.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para sugerencias de productos más sostenibles.
 * 
 * Este DTO contiene productos alternativos recomendados basándose en
 * menor impacto ambiental, misma categoría y precio similar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SugerenciasProductosResponse {
    
    /**
     * ID del producto original para el cual se generaron las sugerencias.
     */
    private Integer productoOriginalId;
    
    /**
     * Nombre del producto original.
     */
    private String productoOriginalNombre;
    
    /**
     * Lista de productos sugeridos ordenados por sostenibilidad (mejor primero).
     */
    private List<ProductoResponseDTO> productosSugeridos;
    
    /**
     * Número total de sugerencias encontradas.
     */
    private Integer totalSugerencias;
    
    /**
     * Criterios utilizados para las sugerencias.
     */
    private String criterios;
}

