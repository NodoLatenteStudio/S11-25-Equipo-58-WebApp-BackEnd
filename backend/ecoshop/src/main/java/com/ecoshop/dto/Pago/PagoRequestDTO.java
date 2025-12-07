package com.ecoshop.dto.Pago;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitar un pago.
 * 
 * Este DTO se usa para crear un intento de pago con cualquier pasarela.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "stripe|paypal|mercadopago", flags = Pattern.Flag.CASE_INSENSITIVE, 
             message = "El método de pago debe ser: stripe, paypal o mercadopago")
    private String metodoPago;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe ser un código de 3 letras (ej: USD, EUR, ARS)")
    private String moneda;

    // Campos opcionales específicos por pasarela
    private String emailCliente; // Para Stripe
    private String returnUrl; // Para PayPal y MercadoPago
    private String cancelUrl; // Para PayPal y MercadoPago
    private String descripcion; // Descripción del pago
}

