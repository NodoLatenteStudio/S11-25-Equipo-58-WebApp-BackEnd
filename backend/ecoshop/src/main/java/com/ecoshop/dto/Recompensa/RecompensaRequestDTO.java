package com.ecoshop.dto.Recompensa;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para crear y actualizar recompensas.
 * 
 * Esta clase representa los datos de una recompensa que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecompensaRequestDTO {

    /**
     * Nombre de la recompensa.
     */
    @NotBlank(message = "El nombre de la recompensa es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    /**
     * Descripción detallada de la recompensa.
     */
    private String descripcion;

    /**
     * Cantidad de eco-puntos necesarios para canjear esta recompensa.
     */
    @NotNull(message = "Los puntos requeridos son obligatorios")
    @Min(value = 1, message = "Los puntos requeridos deben ser al menos 1")
    private Integer puntosRequeridos;

    /**
     * Tipo de recompensa.
     * 
     * Ejemplos: "descuento", "envio_gratis", "producto", "donacion", "experiencia"
     */
    @NotBlank(message = "El tipo de recompensa es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String tipo;

    /**
     * Valor monetario o equivalente de la recompensa.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "El valor debe ser mayor o igual a 0")
    private BigDecimal valor;

    /**
     * Cantidad disponible de la recompensa.
     * 
     * null = ilimitado
     */
    @Min(value = 0, message = "El stock disponible debe ser mayor o igual a 0")
    private Integer stockDisponible;

    /**
     * URL de la imagen de la recompensa.
     */
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String imagenUrl;

    /**
     * Indica si la recompensa está activa y disponible para canje.
     */
    private Boolean activo;
}

