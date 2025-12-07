package com.ecoshop.service.marca;

import com.ecoshop.dto.Marca.DashboardMarcaResponse;
import com.ecoshop.dto.Marca.MetricasAmbientalesMarcaResponse;
import com.ecoshop.dto.Marca.MetricasVentasResponse;

/**
 * Interfaz del servicio para calcular métricas y estadísticas de marcas.
 * 
 * Este servicio proporciona métodos para calcular:
 * - Métricas de ventas (total de ventas, número de pedidos, productos vendidos)
 * - Métricas ambientales (huella de carbono, CO₂ ahorrado, agua ahorrada)
 * - Estadísticas por producto
 * - Dashboard completo con todas las métricas
 */
public interface MarcaMetricasService {
    
    /**
     * Obtiene el dashboard completo de una marca con todas las métricas.
     * 
     * @param marcaId ID de la marca
     * @return Dashboard completo con métricas de ventas, ambientales y estadísticas por producto
     */
    DashboardMarcaResponse obtenerDashboardMarca(Integer marcaId);
    
    /**
     * Obtiene las métricas de ventas de una marca.
     * 
     * @param marcaId ID de la marca
     * @return Métricas de ventas (total de ventas, pedidos, productos vendidos, etc.)
     */
    MetricasVentasResponse obtenerMetricasVentas(Integer marcaId);
    
    /**
     * Obtiene las métricas ambientales agregadas de una marca.
     * 
     * @param marcaId ID de la marca
     * @return Métricas ambientales (huella de carbono, CO₂ ahorrado, agua ahorrada, etc.)
     */
    MetricasAmbientalesMarcaResponse obtenerMetricasAmbientales(Integer marcaId);
}

