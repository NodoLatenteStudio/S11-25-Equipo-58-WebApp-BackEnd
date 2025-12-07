package com.ecoshop.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las métricas ambientales agregadas del usuario.
 * 
 * Versión simplificada del dashboard, enfocada solo en las métricas numéricas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAmbientalesUsuarioResponse {
    
    private BigDecimal co2AhorradoTotal;
    private Integer aguaAhorradaTotal;
    private Integer comprasSostenibles;
    private BigDecimal materialRecicladoTotal;
    private Integer ecoPuntos;
    private String nivelEcoPuntos;
}

