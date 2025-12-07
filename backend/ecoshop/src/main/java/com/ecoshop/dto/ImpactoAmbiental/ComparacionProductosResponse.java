package com.ecoshop.dto.ImpactoAmbiental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para la comparación de impacto ambiental entre productos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionProductosResponse {
    
    private List<ProductoComparacion> productos;
    
    /**
     * Resumen de la comparación
     */
    private Integer productoMenorImpacto;
    private Integer productoMenorPrecio;
    private Integer productoRecomendado;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoComparacion {
        private Integer productoId;
        private String nombre;
        private BigDecimal precio;
        private BigDecimal huellaCarbono;
        private String ecoBadge;
        private String origen;
        private Integer porcentajeReciclable;
        private String materiales;
    }
}

