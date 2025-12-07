package com.ecoshop.service.pago.payment;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.service.pago.PagoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de pagos con MercadoPago.
 * 
 * MercadoPago es una pasarela de pago popular en Latinoamérica.
 * 
 * Flujo:
 * 1. Crear preferencia de pago
 * 2. Frontend redirige a MercadoPago
 * 3. Usuario completa el pago
 * 4. MercadoPago envía webhook con el resultado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoPaymentService implements PagoService {

    private final PedidoRepository pedidoRepository;

    @Value("${mercadopago.access.token:}")
    private String mercadoPagoAccessToken;

    @Value("${mercadopago.public.key:}")
    private String mercadoPagoPublicKey;

    private void configurarMercadoPago() {
        if (mercadoPagoAccessToken == null || mercadoPagoAccessToken.isEmpty()) {
            throw new BadRequestException("MercadoPago no está configurado. Por favor, configure MERCADOPAGO_ACCESS_TOKEN en las variables de entorno.");
        }
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    }

    @Override
    @Transactional
    public PagoResponseDTO crearPago(PagoRequestDTO request) {
        configurarMercadoPago();

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));

        // Validar que el monto coincida
        if (pedido.getTotal().compareTo(request.getMonto()) != 0) {
            throw new BadRequestException("El monto del pago no coincide con el total del pedido");
        }

        try {
            // Crear item de preferencia
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title("Pedido #" + pedido.getPedidoId())
                    .description(request.getDescripcion() != null ? request.getDescripcion() : 
                                "Pago de pedido #" + pedido.getPedidoId())
                    .quantity(1)
                    .currencyId(request.getMoneda().toUpperCase())
                    .unitPrice(request.getMonto())
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(pedido.getPedidoId().toString())
                    .notificationUrl(request.getReturnUrl() != null ? 
                                   request.getReturnUrl() + "/webhook" : 
                                   "https://tu-dominio.com/api/v1/pagos/mercadopago/webhook")
                    .backUrls(com.mercadopago.client.preference.PreferenceBackUrlsRequest.builder()
                            .success(request.getReturnUrl() != null ? request.getReturnUrl() : 
                                    "http://localhost:3000/pago/exito")
                            .failure(request.getCancelUrl() != null ? request.getCancelUrl() : 
                                    "http://localhost:3000/pago/fallido")
                            .pending("http://localhost:3000/pago/pendiente")
                            .build())
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Actualizar pedido
            pedido.setMetodoPago("mercadopago");
            pedido.setIdTransaccionPago(preference.getId());
            pedido.setEstadoPago("procesando");
            pedidoRepository.save(pedido);

            log.info("Preferencia de MercadoPago creada para pedido {}: {}", pedido.getPedidoId(), preference.getId());

            return PagoResponseDTO.builder()
                    .pedidoId(pedido.getPedidoId())
                    .metodoPago("mercadopago")
                    .estadoPago("procesando")
                    .monto(request.getMonto())
                    .moneda(request.getMoneda())
                    .idTransaccionPago(preference.getId())
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .fechaCreacion(LocalDateTime.now())
                    .mensaje("Redirige al usuario a initPoint o sandboxInitPoint según el ambiente")
                    .build();

        } catch (MPException | MPApiException e) {
            log.error("Error al crear preferencia de MercadoPago: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar el pago con MercadoPago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PagoResponseDTO confirmarPago(ConfirmarPagoRequestDTO request) {
        // MercadoPago no requiere confirmación adicional, se confirma vía webhook
        return verificarEstadoPago(request.getPedidoId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO verificarEstadoPago(Integer pedidoId) {
        configurarMercadoPago();

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (pedido.getIdTransaccionPago() == null || !"mercadopago".equalsIgnoreCase(pedido.getMetodoPago())) {
            throw new BadRequestException("Este pedido no tiene un pago de MercadoPago asociado");
        }

        // MercadoPago requiere consultar el estado desde su API
        // Por ahora retornamos el estado actual del pedido
        // En producción, deberías consultar la API de MercadoPago para obtener el estado real

        return PagoResponseDTO.builder()
                .pedidoId(pedidoId)
                .metodoPago("mercadopago")
                .estadoPago(pedido.getEstadoPago())
                .monto(pedido.getTotal())
                .moneda("ARS") // Ajustar según la moneda del pedido
                .idTransaccionPago(pedido.getIdTransaccionPago())
                .fechaCreacion(pedido.getFechaPedido())
                .mensaje("Estado del pago: " + pedido.getEstadoPago())
                .build();
    }

    /**
     * Procesa un webhook de MercadoPago.
     * Este método debe ser llamado desde el controlador de webhooks.
     */
    @Transactional
    public void procesarWebhook(String preferenceId, String status) {
        try {
            Pedido pedido = pedidoRepository.findByIdTransaccionPago(preferenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado para preferencia: " + preferenceId));

            String estadoPago = mapearEstadoMercadoPago(status);
            pedido.setEstadoPago(estadoPago);
            pedidoRepository.save(pedido);

            log.info("Webhook de MercadoPago procesado: status={}, pedido={}, nuevoEstado={}", 
                    status, pedido.getPedidoId(), estadoPago);

        } catch (Exception e) {
            log.error("Error al procesar webhook de MercadoPago: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar webhook: " + e.getMessage());
        }
    }

    /**
     * Mapea el estado de MercadoPago al estado interno del sistema.
     */
    private String mapearEstadoMercadoPago(String estadoMercadoPago) {
        return switch (estadoMercadoPago.toLowerCase()) {
            case "approved", "accredited" -> "completado";
            case "pending", "in_process", "in_mediation" -> "procesando";
            case "rejected", "cancelled", "refunded", "charged_back" -> "fallido";
            default -> "pendiente";
        };
    }
}

