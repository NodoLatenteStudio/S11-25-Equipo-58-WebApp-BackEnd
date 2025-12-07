package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para estadísticas de un producto específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaProductoResponse {
    
    /**
     * ID del producto
     */
    private Integer productoId;
    
    /**
     * Nombre del producto
     */
    private String nombre;
    
    /**
     * Total de unidades vendidas
     */
    private Integer unidadesVendidas;
    
    /**
     * Total de ventas en moneda
     */
    private BigDecimal totalVentas;
    
    /**
     * Número de pedidos que incluyen este producto
     */
    private Integer numeroPedidos;
    
    /**
     * Huella de carbono total del producto vendido (kg CO₂)
     */
    private BigDecimal huellaCarbonoTotal;
    
    /**
     * CO₂ ahorrado total vs convencional (kg CO₂)
     */
    private BigDecimal co2AhorradoTotal;
    
    /**
     * Agua ahorrada total (litros)
     */
    private Integer aguaAhorradaTotal;
    
    /**
     * Eco badge del producto
     */
    private String ecoBadge;
}

