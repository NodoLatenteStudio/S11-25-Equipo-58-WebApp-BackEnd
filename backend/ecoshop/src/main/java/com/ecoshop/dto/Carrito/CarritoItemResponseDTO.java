package com.ecoshop.dto.Carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para respuestas de items del carrito.
 * 
 * Esta clase representa los datos de un item del carrito que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * Campos:
 * - carritoItemId: Identificador único del item
 * - carritoId: ID del carrito al que pertenece el item
 * - productoId: ID del producto
 * - nombreProducto: Nombre del producto (dato útil para mostrar)
 * - imagenUrl: URL de la imagen del producto (dato útil para mostrar)
 * - cantidad: Cantidad del producto en el carrito
 * - precioUnitario: Precio unitario del producto al momento de agregarlo
 * - subtotal: Subtotal calculado (cantidad * precioUnitario)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponseDTO {

    private Integer carritoItemId;
    private Integer carritoId;
    private Integer productoId;
    private String nombreProducto;
    private String imagenUrl;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}

