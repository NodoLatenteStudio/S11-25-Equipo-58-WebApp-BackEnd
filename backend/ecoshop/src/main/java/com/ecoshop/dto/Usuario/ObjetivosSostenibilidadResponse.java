package com.ecoshop.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para los objetivos de sostenibilidad del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivosSostenibilidadResponse {
    
    private Integer usuarioId;
    
    /**
     * Objetivos de CO₂
     */
    private BigDecimal metaCO2;
    private BigDecimal progresoCO2;
    private BigDecimal porcentajeProgresoCO2;
    private BigDecimal co2Restante;
    
    /**
     * Objetivos de Eco-Puntos
     */
    private Integer metaEcoPuntos;
    private Integer progresoEcoPuntos;
    private Double porcentajeProgresoEcoPuntos;
    private Integer ecoPuntosRestantes;
    
    /**
     * Información adicional
     */
    private Boolean objetivoCO2Alcanzado;
    private Boolean objetivoEcoPuntosAlcanzado;
    private String mensajeMotivacional;
}

