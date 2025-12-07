package com.ecoshop.dto.Carrito;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) para respuestas de carrito.
 * 
 * Esta clase representa los datos de un carrito que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * Campos:
 * - carritoId: Identificador único del carrito
 * - usuarioId: ID del usuario propietario del carrito
 * - total: Total del carrito calculado
 * - fechaCreacion: Fecha en que se creó el carrito
 * - fechaActualizacion: Fecha de última actualización
 * - items: Lista de items del carrito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDTO {

    private Integer carritoId;
    private Integer usuarioId;
    private BigDecimal total;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaActualizacion;
    
    private List<CarritoItemResponseDTO> items;
}

