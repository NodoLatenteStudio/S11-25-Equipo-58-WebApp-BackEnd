package com.ecoshop.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para productos paginados.
 * 
 * Este DTO contiene la información de paginación junto con los productos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoPaginadoResponse {
    
    /**
     * Lista de productos en la página actual.
     */
    private List<ProductoResponseDTO> productos;
    
    /**
     * Número de página actual (basado en 0).
     */
    private Integer paginaActual;
    
    /**
     * Tamaño de la página (número de elementos por página).
     */
    private Integer tamanoPagina;
    
    /**
     * Número total de elementos.
     */
    private Long totalElementos;
    
    /**
     * Número total de páginas.
     */
    private Integer totalPaginas;
    
    /**
     * Indica si hay una página siguiente.
     */
    private Boolean tieneSiguiente;
    
    /**
     * Indica si hay una página anterior.
     */
    private Boolean tieneAnterior;
    
    /**
     * Indica si es la primera página.
     */
    private Boolean esPrimera;
    
    /**
     * Indica si es la última página.
     */
    private Boolean esUltima;
}

