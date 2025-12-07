package com.ecoshop.service.pago.payment;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.service.pago.PagoService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de pagos con Stripe.
 * 
 * Stripe es una pasarela de pago que permite procesar pagos con tarjetas de crédito/débito.
 * 
 * Flujo:
 * 1. Crear PaymentIntent con Stripe
 * 2. Frontend usa client_secret para mostrar formulario de pago
 * 3. Stripe procesa el pago
 * 4. Webhook de Stripe notifica el resultado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentService implements PagoService {

    private final PedidoRepository pedidoRepository;

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    @Value("${stripe.public.key:}")
    private String stripePublicKey;

    @Override
    @Transactional
    public PagoResponseDTO crearPago(PagoRequestDTO request) {
        // Validar que Stripe esté configurado
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new BadRequestException("Stripe no está configurado. Por favor, configure STRIPE_SECRET_KEY en las variables de entorno.");
        }

        // Inicializar Stripe
        Stripe.apiKey = stripeSecretKey;

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));

        // Validar que el monto coincida
        if (pedido.getTotal().compareTo(request.getMonto()) != 0) {
            throw new BadRequestException("El monto del pago no coincide con el total del pedido");
        }

        try {
            // Convertir monto a centavos (Stripe usa centavos)
            long montoEnCentavos = request.getMonto().multiply(new BigDecimal("100")).longValue();

            // Crear PaymentIntent
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(montoEnCentavos)
                    .setCurrency(request.getMoneda().toLowerCase())
                    .setDescription(request.getDescripcion() != null ? request.getDescripcion() : 
                                   "Pago de pedido #" + pedido.getPedidoId())
                    .putMetadata("pedido_id", pedido.getPedidoId().toString())
                    .putMetadata("usuario_id", pedido.getUsuario().getUsuarioId().toString());

            PaymentIntentCreateParams params = paramsBuilder.build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Actualizar pedido con información del pago
            pedido.setMetodoPago("stripe");
            pedido.setIdTransaccionPago(paymentIntent.getId());
            pedido.setEstadoPago("procesando");
            pedidoRepository.save(pedido);

            log.info("PaymentIntent creado para pedido {}: {}", pedido.getPedidoId(), paymentIntent.getId());

            // Retornar respuesta
            return PagoResponseDTO.builder()
                    .pedidoId(pedido.getPedidoId())
                    .metodoPago("stripe")
                    .estadoPago("procesando")
                    .monto(request.getMonto())
                    .moneda(request.getMoneda())
                    .idTransaccionPago(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .fechaCreacion(LocalDateTime.now())
                    .mensaje("Usa el clientSecret para procesar el pago en el frontend con Stripe Elements")
                    .build();

        } catch (StripeException e) {
            log.error("Error al crear PaymentIntent con Stripe: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar el pago con Stripe: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PagoResponseDTO confirmarPago(com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO request) {
        // Stripe no requiere confirmación adicional, el pago se confirma automáticamente
        // Este método se usa para verificar el estado después del webhook
        return verificarEstadoPago(request.getPedidoId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO verificarEstadoPago(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (pedido.getIdTransaccionPago() == null || !"stripe".equalsIgnoreCase(pedido.getMetodoPago())) {
            throw new BadRequestException("Este pedido no tiene un pago de Stripe asociado");
        }

        // Inicializar Stripe
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new BadRequestException("Stripe no está configurado");
        }
        Stripe.apiKey = stripeSecretKey;

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(pedido.getIdTransaccionPago());
            
            // Actualizar estado del pedido según el estado del PaymentIntent
            String estadoPago = mapearEstadoStripe(paymentIntent.getStatus());
            pedido.setEstadoPago(estadoPago);
            pedidoRepository.save(pedido);

            return PagoResponseDTO.builder()
                    .pedidoId(pedidoId)
                    .metodoPago("stripe")
                    .estadoPago(estadoPago)
                    .monto(pedido.getTotal())
                    .moneda("usd") // Stripe usa USD por defecto
                    .idTransaccionPago(paymentIntent.getId())
                    .fechaCreacion(pedido.getFechaPedido())
                    .mensaje("Estado del pago: " + estadoPago)
                    .build();

        } catch (StripeException e) {
            log.error("Error al verificar PaymentIntent con Stripe: {}", e.getMessage(), e);
            throw new BadRequestException("Error al verificar el pago con Stripe: " + e.getMessage());
        }
    }

    /**
     * Mapea el estado de Stripe al estado interno del sistema.
     */
    private String mapearEstadoStripe(String estadoStripe) {
        return switch (estadoStripe.toLowerCase()) {
            case "succeeded" -> "completado";
            case "processing", "requires_payment_method", "requires_confirmation", 
                 "requires_action", "requires_capture" -> "procesando";
            case "canceled" -> "cancelado";
            default -> "pendiente";
        };
    }

    /**
     * Procesa un webhook de Stripe.
     * Este método debe ser llamado desde el controlador de webhooks.
     */
    @Transactional
    public void procesarWebhook(String paymentIntentId, String evento) {
        try {
            Pedido pedido = pedidoRepository.findByIdTransaccionPago(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado para transacción: " + paymentIntentId));

            String estadoPago = switch (evento.toLowerCase()) {
                case "payment_intent.succeeded" -> "completado";
                case "payment_intent.payment_failed" -> "fallido";
                case "payment_intent.canceled" -> "cancelado";
                default -> pedido.getEstadoPago();
            };

            pedido.setEstadoPago(estadoPago);
            pedidoRepository.save(pedido);

            log.info("Webhook de Stripe procesado: evento={}, pedido={}, nuevoEstado={}", 
                    evento, pedido.getPedidoId(), estadoPago);

        } catch (Exception e) {
            log.error("Error al procesar webhook de Stripe: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar webhook: " + e.getMessage());
        }
    }
}

