package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para métricas ambientales agregadas de una marca.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAmbientalesMarcaResponse {
    
    /**
     * Huella de carbono total de todos los productos vendidos (kg CO₂)
     */
    private BigDecimal huellaCarbonoTotal;
    
    /**
     * CO₂ ahorrado total vs productos convencionales (kg CO₂)
     */
    private BigDecimal co2AhorradoTotal;
    
    /**
     * Agua ahorrada total (litros)
     */
    private Integer aguaAhorradaTotal;
    
    /**
     * Promedio de huella de carbono por producto vendido (kg CO₂)
     */
    private BigDecimal promedioHuellaCarbonoPorProducto;
    
    /**
     * Distribución de eco-badges en productos vendidos
     */
    private DistribucionEcoBadgeResponse distribucionEcoBadges;
    
    /**
     * Porcentaje promedio de material reciclable en productos vendidos
     */
    private BigDecimal porcentajePromedioReciclable;
}

