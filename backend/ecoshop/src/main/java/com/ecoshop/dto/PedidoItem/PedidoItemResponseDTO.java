package com.ecoshop.dto.PedidoItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para respuestas de items de pedido.
 * 
 * Esta clase representa los datos de un item de pedido que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs de respuesta?
 * - Control sobre qué datos se exponen en la API
 * - Evita exponer información sensible o innecesaria
 * - Permite transformar datos antes de enviarlos al cliente
 * - Facilita versionado de la API
 * - Incluye datos calculados (subtotal) y datos útiles del producto (nombre, imagen)
 * 
 * Campos:
 * - pedidoItemId: Identificador único del item
 * - pedidoId: ID del pedido al que pertenece el item
 * - productoId: ID del producto
 * - nombreProducto: Nombre del producto (dato útil para mostrar)
 * - imagenUrl: URL de la imagen del producto (dato útil para mostrar)
 * - cantidad: Cantidad del producto en el pedido
 * - precioUnitario: Precio unitario del producto al momento de la compra
 * - subtotal: Subtotal calculado (cantidad * precioUnitario)
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class PedidoItemResponseDTO {

    /**
     * Identificador único del item de pedido.
     * 
     * Este ID es generado automáticamente por la base de datos
     * cuando se crea el item.
     */
    private Integer pedidoItemId;

    /**
     * ID del pedido al que pertenece el item.
     * 
     * Referencia al pedido que contiene este item.
     */
    private Integer pedidoId;

    /**
     * ID del producto incluido en el item.
     * 
     * Referencia al producto que se agregó al pedido.
     */
    private Integer productoId;

    /**
     * Nombre del producto.
     * 
     * Dato extra útil para mostrar en el frontend sin necesidad
     * de hacer una consulta adicional al producto.
     */
    private String nombreProducto;

    /**
     * URL de la imagen del producto.
     * 
     * Dato extra útil para mostrar en el frontend sin necesidad
     * de hacer una consulta adicional al producto.
     */
    private String imagenUrl;

    /**
     * Cantidad del producto en el pedido.
     * 
     * Representa la cantidad de unidades del producto que se incluyen en el pedido.
     */
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento de agregarlo al pedido.
     * 
     * Este precio se "congela" al momento de agregar el item al pedido,
     * para que si el precio del producto cambia después, el pedido mantenga
     * el precio original al momento de la compra.
     */
    private BigDecimal precioUnitario;

    /**
     * Subtotal del item calculado.
     * 
     * Se calcula como: cantidad * precioUnitario
     * 
     * Este valor se calcula automáticamente y se incluye en el DTO
     * para facilitar el cálculo del total del pedido en el frontend.
     */
    private BigDecimal subtotal;
}
