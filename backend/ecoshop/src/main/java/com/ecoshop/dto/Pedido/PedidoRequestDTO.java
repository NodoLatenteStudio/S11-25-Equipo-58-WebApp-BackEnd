package com.ecoshop.dto.Pedido;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para crear y actualizar pedidos.
 * 
 * Esta clase representa los datos de un pedido que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @NotBlank, @Pattern, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos:
 * - usuarioId: ID del usuario que realiza el pedido (obligatorio)
 * - estado: Estado del pedido (opcional, valores: pendiente_pago, procesando, enviado, entregado, cancelado)
 * - direccionEnvio: Dirección de envío del pedido (obligatorio)
 * - metodoPago: Método de pago utilizado (opcional)
 * - idTransaccionPago: ID de la transacción de pago (opcional)
 * - huellaCarbonoTotalKg: Huella de carbono total del pedido en kg CO₂ (opcional)
 * 
 * Nota: El total del pedido no se incluye en el DTO porque generalmente se calcula
 * automáticamente a partir de los PedidoItems asociados.
 */
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class PedidoRequestDTO {

    /**
     * ID del usuario que realiza el pedido.
     * 
     * NOTA: Este campo se establece automáticamente desde el usuario autenticado
     * en el controller. Si se proporciona en el body, será sobrescrito por el
     * usuarioId del token JWT para mayor seguridad.
     * 
     * El usuario debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    private Integer usuarioId;

    /**
     * Estado del pedido.
     * 
     * @Pattern: Solo acepta valores específicos: "pendiente_pago", "procesando", "enviado", "entregado", "cancelado"
     * 
     * Campo opcional. Si no se proporciona, el estado por defecto será "pendiente_pago".
     * 
     * Valores posibles:
     * - "pendiente_pago": El pedido está pendiente de pago
     * - "procesando": El pedido está siendo procesado
     * - "enviado": El pedido ha sido enviado
     * - "entregado": El pedido ha sido entregado
     * - "cancelado": El pedido ha sido cancelado
     */
    @Pattern(regexp = "pendiente_pago|procesando|enviado|entregado|cancelado", 
            message = "Estado inválido. Valores permitidos: pendiente_pago, procesando, enviado, entregado, cancelado")
    private String estado;

    /**
     * Dirección de envío del pedido.
     * 
     * @NotBlank: El campo no puede estar vacío ni ser null (solo al crear)
     * 
     * Almacena la dirección completa donde se debe enviar el pedido.
     * Ejemplos: "Av. Principal 123, Santiago, Chile", "Calle 45 #67-89, Bogotá, Colombia"
     * 
     * NOTA: En actualizaciones (PUT), este campo es opcional - si no se proporciona,
     * se mantiene la dirección original.
     */
    private String direccionEnvio;

    /**
     * Método de pago utilizado.
     * 
     * Campo opcional que indica el método de pago utilizado para el pedido.
     * Ejemplos: "tarjeta_credito", "transferencia_bancaria", "paypal", "efectivo"
     */
    private String metodoPago;

    /**
     * ID de la transacción de pago.
     * 
     * Campo opcional que almacena el identificador de la transacción de pago
     * proporcionado por el procesador de pagos (ej: Stripe, PayPal, etc.).
     * Útil para rastrear y verificar pagos.
     */
    private String idTransaccionPago;

    /**
     * Huella de carbono total del pedido en kilogramos de CO₂.
     * 
     * Campo opcional que representa la cantidad total de emisiones de CO₂
     * asociadas al pedido. Se calcula generalmente sumando la huella de carbono
     * de todos los productos incluidos en el pedido.
     * 
     * Ejemplos: 2.5, 5.8, 10.2
     */
    private BigDecimal huellaCarbonoTotalKg;
}
