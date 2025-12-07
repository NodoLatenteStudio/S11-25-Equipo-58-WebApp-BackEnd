package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el dashboard completo de una marca.
 * 
 * Este DTO contiene todas las métricas agregadas de ventas y sostenibilidad
 * para el panel de administración de la marca.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMarcaResponse {
    
    /**
     * Información básica de la marca
     */
    private Integer marcaId;
    private String nombreOficial;
    
    /**
     * Métricas de ventas
     */
    private MetricasVentasResponse metricasVentas;
    
    /**
     * Métricas ambientales agregadas
     */
    private MetricasAmbientalesMarcaResponse metricasAmbientales;
    
    /**
     * Estadísticas por producto
     */
    private List<EstadisticaProductoResponse> estadisticasProductos;
}

