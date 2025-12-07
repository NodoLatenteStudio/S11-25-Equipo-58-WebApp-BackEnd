package com.ecoshop.dto.Usuario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de solicitud para actualizar objetivos de sostenibilidad del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivosSostenibilidadRequest {
    
    /**
     * Meta de CO₂ ahorrado en kg.
     * Debe ser un valor positivo.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "La meta de CO₂ debe ser mayor a 0")
    private BigDecimal metaCO2;
    
    /**
     * Meta personalizada de eco-puntos.
     * Debe ser un valor positivo.
     */
    @Min(value = 1, message = "La meta de eco-puntos debe ser mayor a 0")
    private Integer metaEcoPuntos;
}

