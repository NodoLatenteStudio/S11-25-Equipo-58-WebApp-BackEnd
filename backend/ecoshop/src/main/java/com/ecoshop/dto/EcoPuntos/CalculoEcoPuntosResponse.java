package com.ecoshop.dto.EcoPuntos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el cálculo de eco-puntos de un pedido.
 * 
 * Permite mostrar al usuario cuántos puntos ganará antes de finalizar la compra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoEcoPuntosResponse {
    
    private Integer pedidoId;
    private Integer puntosTotales;
    private Integer puntosBase;
    private Integer puntosBonus;
    private String descripcion;
    private List<ItemPedido> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedido {
        private Integer productoId;
        private String nombreProducto;
        private Integer cantidad;
        private Integer puntos;
        private String motivo; // Por qué se ganaron estos puntos
    }
}

