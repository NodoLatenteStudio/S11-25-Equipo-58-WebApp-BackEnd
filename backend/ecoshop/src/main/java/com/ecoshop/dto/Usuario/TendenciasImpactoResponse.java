package com.ecoshop.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para las tendencias de impacto ambiental del usuario.
 * 
 * Proporciona datos agregados por período (día, semana, mes) para generar gráficas
 * de impacto ambiental a lo largo del tiempo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendenciasImpactoResponse {
    
    private Integer usuarioId;
    private String periodo; // "dia", "semana", "mes"
    
    /**
     * Datos agregados por período para gráficas
     */
    private List<DatoTendencia> datos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatoTendencia {
        /**
         * Etiqueta del período (ej: "2024-01-15", "Semana 3", "Enero 2024")
         */
        private String etiqueta;
        
        /**
         * Fecha de inicio del período
         */
        private String fechaInicio;
        
        /**
         * Fecha de fin del período
         */
        private String fechaFin;
        
        /**
         * CO₂ ahorrado en este período (kg)
         */
        private BigDecimal co2Ahorrado;
        
        /**
         * Agua ahorrada en este período (litros)
         */
        private Integer aguaAhorrada;
        
        /**
         * Huella de carbono total en este período (kg)
         */
        private BigDecimal huellaCarbono;
        
        /**
         * Número de pedidos en este período
         */
        private Integer numeroPedidos;
        
        /**
         * Material reciclado en este período (kg)
         */
        private BigDecimal materialReciclado;
    }
}

