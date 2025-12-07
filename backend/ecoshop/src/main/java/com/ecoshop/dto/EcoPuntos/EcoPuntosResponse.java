package com.ecoshop.dto.EcoPuntos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el estado actual de eco-puntos de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoPuntosResponse {
    
    private Integer usuarioId;
    private Integer ecoPuntos;
    private String nivelEcoPuntos;
    private Integer metaEcoPuntos;
    private Integer puntosParaSiguienteNivel;
    private Double porcentajeProgreso; // Porcentaje hacia el siguiente nivel (0-100)
}

