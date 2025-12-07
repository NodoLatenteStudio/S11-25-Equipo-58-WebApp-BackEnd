package com.ecoshop.dto.ImpactoAmbiental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el impacto ambiental de un producto.
 * 
 * Este DTO se usa para exponer la información de impacto ambiental
 * en las respuestas de la API. La huella de carbono se formatea
 * como string con el formato "X kg CO₂".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactoAmbientalResponse {

    /**
     * Huella de carbono formateada como string.
     * 
     * Formato: "X kg CO₂"
     * Ejemplos: "0.8 kg CO₂", "1.2 kg CO₂", "2.5 kg CO₂"
     */
    private String huellaCarbono;

    /**
     * Indica si los materiales del producto son reciclables.
     * 
     * true: Los materiales son reciclables
     * false: Los materiales no son reciclables
     * null: No se ha especificado esta información
     */
    private Boolean materialesReciclables;

    /**
     * Nivel de impacto ambiental del producto.
     * 
     * Ejemplos: "Bajo impacto ambiental", "Medio impacto ambiental", "Alto impacto ambiental"
     */
    private String nivel;
}

