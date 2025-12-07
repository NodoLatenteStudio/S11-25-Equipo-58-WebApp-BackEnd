package com.ecoshop.dto.ImpactoAmbiental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para el cálculo detallado de impacto ambiental de un pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoImpactoPedidoResponse {
    
    private Integer pedidoId;
    
    /**
     * Desglose de emisiones
     */
    private BigDecimal emisionesProductos;
    private BigDecimal emisionesTransporte;
    private BigDecimal emisionesEntrega;
    
    /**
     * Totales
     */
    private BigDecimal huellaCarbonoTotal;
    private BigDecimal co2AhorradoTotal;
    
    /**
     * Detalle por producto
     */
    private List<ItemPedidoImpacto> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoImpacto {
        private Integer productoId;
        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal huellaCarbonoPorUnidad;
        private BigDecimal huellaCarbonoTotal;
    }
}

