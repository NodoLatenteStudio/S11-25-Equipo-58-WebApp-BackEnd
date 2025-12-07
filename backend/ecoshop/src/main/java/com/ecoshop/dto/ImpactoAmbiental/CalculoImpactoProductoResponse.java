package com.ecoshop.dto.ImpactoAmbiental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el cálculo detallado de impacto ambiental de un producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoImpactoProductoResponse {
    
    private Integer productoId;
    private String nombreProducto;
    
    /**
     * Desglose de emisiones por categoría
     */
    private BigDecimal emisionesFabricacion;
    private BigDecimal emisionesMateriales;
    private BigDecimal emisionesTransporte;
    private BigDecimal emisionesEmpaque;
    
    /**
     * Totales
     */
    private BigDecimal huellaCarbonoTotal;
    private BigDecimal co2AhorradoVsConvencional;
    
    /**
     * Información adicional
     */
    private String origen;
    private Integer porcentajeReciclable;
    private String ecoBadge;
}

