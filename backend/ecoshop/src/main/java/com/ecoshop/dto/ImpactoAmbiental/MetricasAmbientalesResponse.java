package com.ecoshop.dto.ImpactoAmbiental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las métricas ambientales detalladas de un producto.
 * 
 * Este DTO proporciona un desglose completo de todas las métricas ambientales
 * asociadas a un producto, incluyendo emisiones por categoría, consumo de agua,
 * y comparaciones con productos convencionales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAmbientalesResponse {
    
    private Integer productoId;
    private String nombreProducto;
    
    /**
     * Desglose de emisiones de CO₂ por categoría (en kg)
     */
    private BigDecimal emisionesFabricacion;
    private BigDecimal emisionesMateriales;
    private BigDecimal emisionesTransporte;
    private BigDecimal emisionesEmpaque;
    private BigDecimal emisionesEntrega;
    
    /**
     * Totales y comparaciones
     */
    private BigDecimal huellaCarbonoTotal;
    private BigDecimal co2AhorradoVsConvencional;
    
    /**
     * Métricas adicionales
     */
    private Integer consumoAgua; // Litros
    private Integer distanciaTransporte; // Kilómetros
    private Integer porcentajeReciclable; // Porcentaje 0-100
    private String ecoBadge;
    private String origen;
    private String materiales;
    
    /**
     * Equivalencias para facilitar la comprensión
     */
    private String equivalenteAgua; // Ej: "Equivalente a X duchas"
    private String equivalenteCO2; // Ej: "Equivalente a X km en auto"
}

