package com.ecoshop.dto.Canje;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de canjes.
 * 
 * Esta clase representa los datos de un canje que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanjeResponseDTO {

    /**
     * Identificador único del canje.
     */
    private Integer canjeId;

    /**
     * ID del usuario que realizó el canje.
     */
    private Integer usuarioId;

    /**
     * ID de la recompensa canjeada.
     */
    private Integer recompensaId;

    /**
     * Nombre de la recompensa canjeada.
     */
    private String nombreRecompensa;

    /**
     * Cantidad de eco-puntos utilizados en este canje.
     */
    private Integer puntosUsados;

    /**
     * Estado del canje.
     * 
     * Valores posibles: "pendiente", "completado", "cancelado", "expirado"
     */
    private String estado;

    /**
     * Código único del canje para validación.
     */
    private String codigoCanje;

    /**
     * Fecha en que se realizó el canje.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCanje;
}
