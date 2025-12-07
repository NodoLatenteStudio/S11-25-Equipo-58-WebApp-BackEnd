package com.ecoshop.dto.Carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para el impacto ambiental del carrito.
 * 
 * Esta clase representa el impacto ambiental calculado del carrito actual,
 * incluyendo huella de carbono, CO₂ ahorrado y equivalencias.
 * 
 * Campos:
 * - huellaCarbonoTotal: Huella de carbono total del carrito en kg CO₂
 * - co2Ahorrado: CO₂ ahorrado vs productos convencionales
 * - equivalente: Equivalencia en términos comprensibles (ej: "equivalente a 2 km en auto")
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactoAmbientalCarritoResponse {

    private BigDecimal huellaCarbonoTotal;
    private BigDecimal co2Ahorrado;
    private String equivalente;
}

