package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para marcas paginadas.
 * 
 * Este DTO contiene la información de paginación junto con las marcas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarcaPaginadoResponse {
    
    /**
     * Lista de marcas en la página actual.
     */
    private List<MarcaResponseDTO> marcas;
    
    /**
     * Número de página actual (basado en 1).
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

