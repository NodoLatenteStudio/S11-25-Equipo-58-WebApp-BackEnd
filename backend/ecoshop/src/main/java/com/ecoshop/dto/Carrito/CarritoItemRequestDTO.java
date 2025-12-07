package com.ecoshop.dto.Carrito;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear o actualizar items del carrito.
 * 
 * Esta clase representa los datos de un item del carrito que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) o actualización (PUT).
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @Min, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * 
 * Campos:
 * - productoId: ID del producto que se agrega al carrito (obligatorio)
 * - cantidad: Cantidad del producto (obligatorio, mínimo 1)
 * 
 * Nota: El precio_unitario no se incluye en el DTO porque se obtiene
 * automáticamente del producto al momento de agregarlo al carrito.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemRequestDTO {

    /**
     * ID del producto que se agrega al carrito.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * 
     * El producto debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    /**
     * Cantidad del producto en el carrito.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * @Min: El valor debe ser al menos 1 (no puede ser 0 ni negativo)
     * 
     * Representa la cantidad de unidades del producto que se incluyen en el carrito.
     * 
     * Ejemplos válidos: 1, 2, 5, 10
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}

