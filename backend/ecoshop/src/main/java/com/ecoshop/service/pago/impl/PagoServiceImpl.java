package com.ecoshop.service.pago.impl;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.service.pago.PagoService;
import com.ecoshop.service.pago.payment.StripePaymentService;
import com.ecoshop.service.pago.payment.PayPalPaymentService;
import com.ecoshop.service.pago.payment.MercadoPagoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación principal del servicio de pagos.
 * 
 * Este servicio actúa como un router que delega a la pasarela específica
 * según el método de pago solicitado o almacenado en el pedido.
 * 
 * Funcionalidades:
 * - Crear pagos con diferentes pasarelas
 * - Confirmar pagos obteniendo el método de pago del pedido
 * - Verificar estado de pagos obteniendo el método de pago del pedido
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final MercadoPagoPaymentService mercadoPagoPaymentService;
    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional
    public PagoResponseDTO crearPago(PagoRequestDTO request) {
        if (request.getMetodoPago() == null || request.getMetodoPago().trim().isEmpty()) {
            throw new BadRequestException("El método de pago es obligatorio");
        }

        String metodoPago = request.getMetodoPago().toLowerCase().trim();
        
        return switch (metodoPago) {
            case "stripe" -> stripePaymentService.crearPago(request);
            case "paypal" -> payPalPaymentService.crearPago(request);
            case "mercadopago" -> mercadoPagoPaymentService.crearPago(request);
            default -> throw new BadRequestException("Método de pago no soportado: " + request.getMetodoPago() + 
                    ". Métodos soportados: stripe, paypal, mercadopago");
        };
    }

    @Override
    @Transactional
    public PagoResponseDTO confirmarPago(ConfirmarPagoRequestDTO request) {
        if (request.getPedidoId() == null) {
            throw new BadRequestException("El ID del pedido es obligatorio");
        }

        // Buscar el pedido para obtener el método de pago
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));

        if (pedido.getMetodoPago() == null || pedido.getMetodoPago().trim().isEmpty()) {
            throw new BadRequestException("El pedido no tiene un método de pago asociado");
        }

        String metodoPago = pedido.getMetodoPago().toLowerCase().trim();
        
        log.info("Confirmando pago para pedido {} con método {}", request.getPedidoId(), metodoPago);

        return switch (metodoPago) {
            case "stripe" -> stripePaymentService.confirmarPago(request);
            case "paypal" -> payPalPaymentService.confirmarPago(request);
            case "mercadopago" -> mercadoPagoPaymentService.confirmarPago(request);
            default -> throw new BadRequestException("Método de pago no soportado: " + pedido.getMetodoPago() + 
                    ". Métodos soportados: stripe, paypal, mercadopago");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO verificarEstadoPago(Integer pedidoId) {
        if (pedidoId == null) {
            throw new BadRequestException("El ID del pedido es obligatorio");
        }

        // Buscar el pedido para obtener el método de pago
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (pedido.getMetodoPago() == null || pedido.getMetodoPago().trim().isEmpty()) {
            throw new BadRequestException("El pedido no tiene un método de pago asociado");
        }

        String metodoPago = pedido.getMetodoPago().toLowerCase().trim();
        
        log.info("Verificando estado de pago para pedido {} con método {}", pedidoId, metodoPago);

        return switch (metodoPago) {
            case "stripe" -> stripePaymentService.verificarEstadoPago(pedidoId);
            case "paypal" -> payPalPaymentService.verificarEstadoPago(pedidoId);
            case "mercadopago" -> mercadoPagoPaymentService.verificarEstadoPago(pedidoId);
            default -> throw new BadRequestException("Método de pago no soportado: " + pedido.getMetodoPago() + 
                    ". Métodos soportados: stripe, paypal, mercadopago");
        };
    }
}