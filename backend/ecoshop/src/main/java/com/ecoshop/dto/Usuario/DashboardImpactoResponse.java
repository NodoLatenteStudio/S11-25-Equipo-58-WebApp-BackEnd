package com.ecoshop.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el dashboard de impacto ambiental del usuario.
 * 
 * Este DTO proporciona un resumen completo del impacto ambiental del usuario,
 * incluyendo CO₂ ahorrado, agua ahorrada, compras sostenibles, eco-puntos y objetivos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardImpactoResponse {
    
    /**
     * CO₂ ahorrado total en todas las compras (kg)
     */
    private BigDecimal co2AhorradoTotal;
    
    /**
     * Equivalente del CO₂ ahorrado en términos comprensibles
     * Ej: "Equivalente a plantar 4 árboles"
     */
    private String co2AhorradoEquivalente;
    
    /**
     * Agua ahorrada total en todas las compras (litros)
     */
    private Integer aguaAhorradaTotal;
    
    /**
     * Equivalente del agua ahorrada en términos comprensibles
     * Ej: "Equivalente a 12 duchas"
     */
    private String aguaAhorradaEquivalente;
    
    /**
     * Número de compras sostenibles realizadas
     */
    private Integer comprasSostenibles;
    
    /**
     * Material reciclado total (kg)
     */
    private BigDecimal materialRecicladoTotal;
    
    /**
     * Eco-puntos acumulados
     */
    private Integer ecoPuntos;
    
    /**
     * Nivel actual de eco-puntos
     * Ej: "Bronce", "Plata", "Oro", "Platino"
     */
    private String nivelEcoPuntos;
    
    /**
     * Objetivos de sostenibilidad del usuario
     */
    private ObjetivosSostenibilidad objetivos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjetivosSostenibilidad {
        /**
         * Meta de CO₂ ahorrado (kg)
         */
        private BigDecimal co2Meta;
        
        /**
         * Progreso actual de CO₂ ahorrado (kg)
         */
        private BigDecimal co2Progreso;
        
        /**
         * Meta de eco-puntos
         */
        private Integer ecoPuntosMeta;
        
        /**
         * Progreso actual de eco-puntos
         */
        private Integer ecoPuntosProgreso;
    }
}

