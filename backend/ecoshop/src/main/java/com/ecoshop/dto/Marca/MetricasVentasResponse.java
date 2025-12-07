package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para métricas de ventas de una marca.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasVentasResponse {
    
    /**
     * Total de ventas en moneda
     */
    private BigDecimal totalVentas;
    
    /**
     * Número total de pedidos que incluyen productos de la marca
     */
    private Integer totalPedidos;
    
    /**
     * Número total de productos vendidos (suma de cantidades)
     */
    private Integer totalProductosVendidos;
    
    /**
     * Número de productos únicos vendidos
     */
    private Integer productosUnicosVendidos;
    
    /**
     * Promedio de venta por pedido
     */
    private BigDecimal promedioVentaPorPedido;
    
    /**
     * Total de productos en el catálogo de la marca
     */
    private Integer totalProductosCatalogo;
}

