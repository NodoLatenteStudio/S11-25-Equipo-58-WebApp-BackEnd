package com.ecoshop.dto.PedidoItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear items de pedido.
 * 
 * Esta clase representa los datos de un item de pedido que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @Min, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos:
 * - pedidoId: ID del pedido al que pertenece el item (obligatorio)
 * - productoId: ID del producto que se agrega al pedido (obligatorio)
 * - cantidad: Cantidad del producto (obligatorio, mínimo 1)
 * 
 * Nota: El precio_unitario no se incluye en el DTO porque se obtiene
 * automáticamente del producto al momento de agregarlo al pedido.
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class PedidoItemRequestDTO {

    /**
     * ID del pedido al que pertenece el item.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * 
     * El pedido debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    /**
     * ID del producto que se agrega al pedido.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * 
     * El producto debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    /**
     * Cantidad del producto en el pedido.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * @Min: El valor debe ser al menos 1 (no puede ser 0 ni negativo)
     * 
     * Representa la cantidad de unidades del producto que se incluyen en el pedido.
     * 
     * Ejemplos válidos: 1, 2, 5, 10
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}
