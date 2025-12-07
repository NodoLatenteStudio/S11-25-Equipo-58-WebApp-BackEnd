package com.ecoshop.dto.Pago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para confirmar un pago.
 * 
 * Se usa para confirmar pagos que requieren confirmación adicional
 * (como PayPal que requiere captura después de la aprobación).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarPagoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotBlank(message = "El ID de la transacción es obligatorio")
    private String idTransaccionPago;

    // Campos opcionales específicos por pasarela
    private String payerId; // Para PayPal
    private String paymentId; // Para PayPal
}

