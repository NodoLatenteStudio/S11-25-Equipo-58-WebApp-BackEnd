package com.ecoshop.dto.Inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el stock de un producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarStockRequestDTO {

    @NotNull(message = "El nuevo stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer nuevoStock;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento; // "entrada", "salida", "ajuste"

    private String motivo; // Opcional: motivo del cambio
}

