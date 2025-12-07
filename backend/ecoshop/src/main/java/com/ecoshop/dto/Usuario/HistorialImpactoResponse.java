package com.ecoshop.dto.Usuario;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para el historial de impacto ambiental del usuario.
 * 
 * Proporciona un historial detallado de compras con impacto acumulado,
 * permitiendo filtrar por rango de fechas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialImpactoResponse {
    
    private Integer usuarioId;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaInicio;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaFin;
    
    /**
     * Resumen total del período
     */
    private ResumenPeriodo resumen;
    
    /**
     * Lista de pedidos con su impacto ambiental
     */
    private List<PedidoImpacto> pedidos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenPeriodo {
        private Integer totalPedidos;
        private BigDecimal co2AhorradoTotal;
        private Integer aguaAhorradaTotal;
        private BigDecimal materialRecicladoTotal;
        private BigDecimal huellaCarbonoTotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoImpacto {
        private Integer pedidoId;
        
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        private LocalDateTime fechaPedido;
        
        private String estado;
        private BigDecimal total;
        private BigDecimal huellaCarbono;
        private BigDecimal co2Ahorrado;
        private Integer aguaAhorrada;
        private Integer cantidadProductos;
        private List<String> productos; // Nombres de productos
    }
}

