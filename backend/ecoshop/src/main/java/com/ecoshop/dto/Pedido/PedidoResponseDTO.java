package com.ecoshop.dto.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de pedidos.
 * 
 * Esta clase representa los datos de un pedido que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs de respuesta?
 * - Control sobre qué datos se exponen en la API
 * - Evita exponer información sensible o innecesaria
 * - Permite transformar datos antes de enviarlos al cliente
 * - Facilita versionado de la API
 * 
 * Campos:
 * - pedidoId: Identificador único del pedido
 * - usuarioId: ID del usuario que realizó el pedido
 * - emailUsuario: Email del usuario (dato extra útil para administradores)
 * - fechaPedido: Fecha en que se realizó el pedido
 * - estado: Estado actual del pedido
 * - total: Total del pedido en moneda
 * - direccionEnvio: Dirección de envío del pedido
 * - metodoPago: Método de pago utilizado
 * - idTransaccionPago: ID de la transacción de pago
 * - huellaCarbonoTotalKg: Huella de carbono total del pedido en kg CO₂
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class PedidoResponseDTO {

    /**
     * Identificador único del pedido.
     * 
     * Este ID es generado automáticamente por la base de datos
     * cuando se crea el pedido.
     */
    private Integer pedidoId;

    /**
     * ID del usuario que realizó el pedido.
     * 
     * Referencia al usuario que creó este pedido.
     */
    private Integer usuarioId;

    /**
     * Email del usuario que realizó el pedido.
     * 
     * Dato extra útil para administradores que necesitan identificar
     * rápidamente al usuario sin hacer una consulta adicional.
     */
    private String emailUsuario;

    /**
     * Fecha en que se realizó el pedido.
     * 
     * Esta fecha se establece automáticamente cuando se crea el pedido
     * y no se puede modificar posteriormente.
     */
    private LocalDateTime fechaPedido;

    /**
     * Estado actual del pedido.
     * 
     * Valores posibles:
     * - "pendiente_pago": El pedido está pendiente de pago
     * - "procesando": El pedido está siendo procesado
     * - "enviado": El pedido ha sido enviado
     * - "entregado": El pedido ha sido entregado
     * - "cancelado": El pedido ha sido cancelado
     */
    private String estado;

    /**
     * Total del pedido en moneda.
     * 
     * Representa la suma total de todos los items del pedido.
     * Ejemplos: 14990.00, 15990.50, 9999.99
     */
    private BigDecimal total;

    /**
     * Dirección de envío del pedido.
     * 
     * Dirección completa donde se debe enviar el pedido.
     */
    private String direccionEnvio;

    /**
     * Método de pago utilizado.
     * 
     * Ejemplos: "tarjeta_credito", "transferencia_bancaria", "paypal", "efectivo"
     */
    private String metodoPago;

    /**
     * ID de la transacción de pago.
     * 
     * Identificador de la transacción de pago proporcionado por el procesador de pagos.
     * Útil para rastrear y verificar pagos.
     */
    private String idTransaccionPago;

    /**
     * Huella de carbono total del pedido en kilogramos de CO₂.
     * 
     * Representa la cantidad total de emisiones de CO₂ asociadas al pedido.
     * Se calcula generalmente sumando la huella de carbono de todos los productos.
     */
    private BigDecimal huellaCarbonoTotalKg;
}
