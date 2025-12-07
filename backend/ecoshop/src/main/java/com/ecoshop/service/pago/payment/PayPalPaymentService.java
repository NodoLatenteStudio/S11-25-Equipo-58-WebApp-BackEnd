package com.ecoshop.service.pago.payment;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.service.pago.PagoService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de pagos con PayPal.
 * 
 * PayPal es una pasarela de pago que permite pagos con cuenta PayPal o tarjetas.
 * 
 * Flujo:
 * 1. Crear orden de PayPal
 * 2. Frontend redirige a PayPal para aprobación
 * 3. Usuario aprueba en PayPal
 * 4. PayPal redirige de vuelta con código de autorización
 * 5. Capturar el pago con el código de autorización
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayPalPaymentService implements PagoService {

    private final PedidoRepository pedidoRepository;

    @Value("${paypal.client.id:}")
    private String paypalClientId;

    @Value("${paypal.client.secret:}")
    private String paypalClientSecret;

    @Value("${paypal.mode:sandbox}")
    private String paypalMode; // sandbox o live

    private APIContext getApiContext() {
        if (paypalClientId == null || paypalClientId.isEmpty() || 
            paypalClientSecret == null || paypalClientSecret.isEmpty()) {
            throw new BadRequestException("PayPal no está configurado. Por favor, configure PAYPAL_CLIENT_ID y PAYPAL_CLIENT_SECRET en las variables de entorno.");
        }
        return new APIContext(paypalClientId, paypalClientSecret, paypalMode);
    }

    @Override
    @Transactional
    public PagoResponseDTO crearPago(PagoRequestDTO request) {
        APIContext apiContext = getApiContext();

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));

        // Validar que el monto coincida
        if (pedido.getTotal().compareTo(request.getMonto()) != 0) {
            throw new BadRequestException("El monto del pago no coincide con el total del pedido");
        }

        try {
            // Crear Amount
            Amount amount = new Amount();
            amount.setCurrency(request.getMoneda().toUpperCase());
            amount.setTotal(String.format("%.2f", request.getMonto()));

            // Crear Transaction
            Transaction transaction = new Transaction();
            transaction.setDescription(request.getDescripcion() != null ? request.getDescripcion() : 
                                      "Pago de pedido #" + pedido.getPedidoId());
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            // Crear Payer
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            // Crear Payment
            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            // Configurar Redirect URLs
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : 
                                     "http://localhost:3000/pago/exito");
            redirectUrls.setCancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : 
                                     "http://localhost:3000/pago/cancelado");
            payment.setRedirectUrls(redirectUrls);

            // Crear el pago en PayPal
            Payment createdPayment = payment.create(apiContext);

            // Obtener URL de aprobación
            String approvalUrl = null;
            for (Links link : createdPayment.getLinks()) {
                if ("approval_url".equals(link.getRel())) {
                    approvalUrl = link.getHref();
                    break;
                }
            }

            if (approvalUrl == null) {
                throw new BadRequestException("No se pudo obtener la URL de aprobación de PayPal");
            }

            // Actualizar pedido
            pedido.setMetodoPago("paypal");
            pedido.setIdTransaccionPago(createdPayment.getId());
            pedido.setEstadoPago("procesando");
            pedidoRepository.save(pedido);

            log.info("Orden de PayPal creada para pedido {}: {}", pedido.getPedidoId(), createdPayment.getId());

            return PagoResponseDTO.builder()
                    .pedidoId(pedido.getPedidoId())
                    .metodoPago("paypal")
                    .estadoPago("procesando")
                    .monto(request.getMonto())
                    .moneda(request.getMoneda())
                    .idTransaccionPago(createdPayment.getId())
                    .approvalUrl(approvalUrl)
                    .fechaCreacion(LocalDateTime.now())
                    .mensaje("Redirige al usuario a la URL de aprobación de PayPal")
                    .build();

        } catch (PayPalRESTException e) {
            log.error("Error al crear orden de PayPal: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar el pago con PayPal: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PagoResponseDTO confirmarPago(ConfirmarPagoRequestDTO request) {
        APIContext apiContext = getApiContext();

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));

        if (!"paypal".equalsIgnoreCase(pedido.getMetodoPago()) || 
            !pedido.getIdTransaccionPago().equals(request.getIdTransaccionPago())) {
            throw new BadRequestException("El ID de transacción no coincide con el pedido");
        }

        try {
            // Obtener el pago original
            Payment payment = Payment.get(apiContext, request.getIdTransaccionPago());

            // Crear PaymentExecution con el payerId
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(request.getPayerId() != null ? request.getPayerId() : 
                                       request.getPaymentId());

            // Ejecutar (capturar) el pago
            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            // Actualizar estado del pedido
            String estadoPago = mapearEstadoPayPal(executedPayment.getState());
            pedido.setEstadoPago(estadoPago);
            pedidoRepository.save(pedido);

            log.info("Pago de PayPal capturado para pedido {}: {}", pedido.getPedidoId(), executedPayment.getState());

            return PagoResponseDTO.builder()
                    .pedidoId(pedido.getPedidoId())
                    .metodoPago("paypal")
                    .estadoPago(estadoPago)
                    .monto(pedido.getTotal())
                    .moneda("USD") // PayPal usa USD por defecto
                    .idTransaccionPago(executedPayment.getId())
                    .fechaCreacion(pedido.getFechaPedido())
                    .mensaje("Pago " + estadoPago)
                    .build();

        } catch (PayPalRESTException e) {
            log.error("Error al capturar pago de PayPal: {}", e.getMessage(), e);
            throw new BadRequestException("Error al confirmar el pago con PayPal: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO verificarEstadoPago(Integer pedidoId) {
        APIContext apiContext = getApiContext();

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (pedido.getIdTransaccionPago() == null || !"paypal".equalsIgnoreCase(pedido.getMetodoPago())) {
            throw new BadRequestException("Este pedido no tiene un pago de PayPal asociado");
        }

        try {
            Payment payment = Payment.get(apiContext, pedido.getIdTransaccionPago());
            String estadoPago = mapearEstadoPayPal(payment.getState());

            // Actualizar estado del pedido
            pedido.setEstadoPago(estadoPago);
            pedidoRepository.save(pedido);

            return PagoResponseDTO.builder()
                    .pedidoId(pedidoId)
                    .metodoPago("paypal")
                    .estadoPago(estadoPago)
                    .monto(pedido.getTotal())
                    .moneda("USD")
                    .idTransaccionPago(payment.getId())
                    .fechaCreacion(pedido.getFechaPedido())
                    .mensaje("Estado del pago: " + estadoPago)
                    .build();

        } catch (PayPalRESTException e) {
            log.error("Error al verificar pago de PayPal: {}", e.getMessage(), e);
            throw new BadRequestException("Error al verificar el pago con PayPal: " + e.getMessage());
        }
    }

    /**
     * Mapea el estado de PayPal al estado interno del sistema.
     */
    private String mapearEstadoPayPal(String estadoPayPal) {
        return switch (estadoPayPal.toLowerCase()) {
            case "approved", "completed" -> "completado";
            case "pending", "in_progress" -> "procesando";
            case "failed", "denied" -> "fallido";
            case "canceled", "cancelled" -> "cancelado";
            default -> "pendiente";
        };
    }
}

