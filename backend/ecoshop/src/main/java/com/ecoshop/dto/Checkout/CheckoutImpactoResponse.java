package com.ecoshop.dto.Checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para el cálculo de impacto ambiental en checkout.
 * 
 * Esta clase representa el impacto ambiental completo que tendrá un pedido
 * antes de finalizar la compra, incluyendo huella de carbono, CO₂ ahorrado,
 * eco-puntos que se ganarán, agua ahorrada y equivalencias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutImpactoResponse {

    /**
     * Huella de carbono total del pedido en kg CO₂.
     */
    private BigDecimal huellaCarbono;

    /**
     * CO₂ ahorrado vs productos convencionales en kg CO₂.
     */
    private BigDecimal co2Ahorrado;

    /**
     * Equivalencia del CO₂ ahorrado en términos comprensibles.
     * Ejemplo: "Equivalente a 2 km en auto"
     */
    private String co2AhorradoEquivalente;

    /**
     * Eco-puntos que se ganarán al completar la compra.
     */
    private Integer ecoPuntosAGanar;

    /**
     * Agua ahorrada en litros.
     */
    private Integer aguaAhorrada;

    /**
     * Equivalencia del agua ahorrada en términos comprensibles.
     * Ejemplo: "Equivalente a 1 ducha de 5 minutos"
     */
    private String aguaAhorradaEquivalente;
}

