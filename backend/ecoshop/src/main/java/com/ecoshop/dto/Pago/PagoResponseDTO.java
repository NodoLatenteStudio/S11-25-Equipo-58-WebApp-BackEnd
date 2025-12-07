package com.ecoshop.dto.Pago;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de pago.
 * 
 * Contiene la información necesaria para que el frontend procese el pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {

    private Integer pedidoId;
    private String metodoPago;
    private String estadoPago;
    private BigDecimal monto;
    private String moneda;
    private String idTransaccionPago;

    // Campos específicos por pasarela
    private String clientSecret; // Para Stripe (PaymentIntent client_secret)
    private String approvalUrl; // Para PayPal (URL de aprobación)
    private String preferenceId; // Para MercadoPago (ID de preferencia)
    private String initPoint; // Para MercadoPago (URL de pago)
    private String sandboxInitPoint; // Para MercadoPago (URL de pago en sandbox)

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;

    private String mensaje; // Mensaje informativo para el usuario
}

