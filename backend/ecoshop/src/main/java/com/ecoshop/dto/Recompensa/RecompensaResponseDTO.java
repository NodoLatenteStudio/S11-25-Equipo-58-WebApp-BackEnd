package com.ecoshop.dto.Recompensa;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de recompensas.
 * 
 * Esta clase representa los datos de una recompensa que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecompensaResponseDTO {

    /**
     * Identificador único de la recompensa.
     */
    private Integer recompensaId;

    /**
     * Nombre de la recompensa.
     */
    private String nombre;

    /**
     * Descripción detallada de la recompensa.
     */
    private String descripcion;

    /**
     * Cantidad de eco-puntos necesarios para canjear esta recompensa.
     */
    private Integer puntosRequeridos;

    /**
     * Tipo de recompensa.
     */
    private String tipo;

    /**
     * Valor monetario o equivalente de la recompensa.
     */
    private BigDecimal valor;

    /**
     * Cantidad disponible de la recompensa.
     * null = ilimitado
     */
    private Integer stockDisponible;

    /**
     * URL de la imagen de la recompensa.
     */
    private String imagenUrl;

    /**
     * Indica si la recompensa está activa y disponible para canje.
     */
    private Boolean activo;

    /**
     * Fecha de creación de la recompensa.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización de la recompensa.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaActualizacion;
}

