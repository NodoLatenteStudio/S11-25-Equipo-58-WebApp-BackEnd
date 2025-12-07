package com.ecoshop.service.pago;

import com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;

/**
 * Interfaz del servicio de pagos.
 * 
 * Define el contrato para procesar pagos con diferentes pasarelas.
 */
public interface PagoService {

    /**
     * Crea un intento de pago con la pasarela especificada.
     * 
     * @param request Datos del pago (pedido, método, monto, etc.)
     * @return PagoResponseDTO con la información necesaria para procesar el pago en el frontend
     */
    PagoResponseDTO crearPago(PagoRequestDTO request);

    /**
     * Confirma un pago pendiente.
     * 
     * Útil para pasarelas que requieren confirmación adicional
     * (ej: PayPal requiere captura después de la aprobación).
     * 
     * @param request Datos de confirmación del pago
     * @return PagoResponseDTO con el estado actualizado del pago
     */
    PagoResponseDTO confirmarPago(ConfirmarPagoRequestDTO request);

    /**
     * Verifica el estado de un pago.
     * 
     * @param pedidoId ID del pedido
     * @return PagoResponseDTO con el estado actual del pago
     */
    PagoResponseDTO verificarEstadoPago(Integer pedidoId);
}

