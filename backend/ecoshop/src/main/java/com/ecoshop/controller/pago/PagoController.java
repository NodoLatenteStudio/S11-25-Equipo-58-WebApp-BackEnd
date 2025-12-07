package com.ecoshop.controller.pago;

import com.ecoshop.dto.Pago.ConfirmarPagoRequestDTO;
import com.ecoshop.dto.Pago.PagoRequestDTO;
import com.ecoshop.dto.Pago.PagoResponseDTO;
import com.ecoshop.service.pago.PagoService;
import com.ecoshop.service.pago.payment.MercadoPagoPaymentService;
import com.ecoshop.service.pago.payment.StripePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestionar pagos con diferentes pasarelas.
 * 
 * Este controlador expone los endpoints HTTP para procesar pagos con:
 * - Stripe
 * - PayPal
 * - MercadoPago
 * 
 * Todos los endpoints están bajo la ruta base "/api/v1/pagos".
 * 
 * Nota: El usuario debe estar autenticado para acceder a estos endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final StripePaymentService stripePaymentService;
    private final MercadoPagoPaymentService mercadoPagoPaymentService;

    /**
     * Crea un intento de pago con la pasarela especificada.
     * 
     * Endpoint: POST /api/v1/pagos/crear
     * 
     * @param request Datos del pago (pedido, método, monto, etc.)
     * @return ResponseEntity con la información necesaria para procesar el pago
     */
    @PostMapping("/crear")
    public ResponseEntity<PagoResponseDTO> crearPago(@Valid @RequestBody PagoRequestDTO request) {
        PagoResponseDTO response = pagoService.crearPago(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Crea un intento de pago con Stripe.
     * 
     * Endpoint: POST /api/v1/pagos/stripe/crear-intento
     * 
     * @param request Datos del pago
     * @return ResponseEntity con clientSecret para procesar el pago en el frontend
     */
    @PostMapping("/stripe/crear-intento")
    public ResponseEntity<PagoResponseDTO> crearPagoStripe(@Valid @RequestBody PagoRequestDTO request) {
        request.setMetodoPago("stripe");
        PagoResponseDTO response = pagoService.crearPago(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Confirma un pago de Stripe (verifica estado).
     * 
     * Endpoint: POST /api/v1/pagos/stripe/confirmar
     * 
     * @param request Datos de confirmación
     * @return ResponseEntity con el estado actualizado del pago
     */
    @PostMapping("/stripe/confirmar")
    public ResponseEntity<PagoResponseDTO> confirmarPagoStripe(@Valid @RequestBody ConfirmarPagoRequestDTO request) {
        PagoResponseDTO response = stripePaymentService.confirmarPago(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook de Stripe para recibir notificaciones de pago.
     * 
     * Endpoint: POST /api/v1/pagos/stripe/webhook
     * 
     * @param payload Payload del webhook de Stripe
     * @return ResponseEntity con estado 200 si se procesó correctamente
     */
    @PostMapping("/stripe/webhook")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, String>> webhookStripe(@RequestBody Map<String, Object> payload) {
        try {
            String tipo = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");

            if ("payment_intent.succeeded".equals(tipo) || 
                "payment_intent.payment_failed".equals(tipo) ||
                "payment_intent.canceled".equals(tipo)) {
                String paymentIntentId = (String) object.get("id");
                String evento = tipo;
                
                stripePaymentService.procesarWebhook(paymentIntentId, evento);
            }

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Error al procesar webhook de Stripe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Crea una orden de pago con PayPal.
     * 
     * Endpoint: POST /api/v1/pagos/paypal/crear-orden
     * 
     * @param request Datos del pago
     * @return ResponseEntity con approvalUrl para redirigir al usuario
     */
    @PostMapping("/paypal/crear-orden")
    public ResponseEntity<PagoResponseDTO> crearOrdenPayPal(@Valid @RequestBody PagoRequestDTO request) {
        request.setMetodoPago("paypal");
        PagoResponseDTO response = pagoService.crearPago(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Captura un pago de PayPal después de la aprobación.
     * 
     * Endpoint: POST /api/v1/pagos/paypal/capturar
     * 
     * @param request Datos de confirmación (incluye payerId y paymentId)
     * @return ResponseEntity con el estado actualizado del pago
     */
    @PostMapping("/paypal/capturar")
    public ResponseEntity<PagoResponseDTO> capturarPagoPayPal(@Valid @RequestBody ConfirmarPagoRequestDTO request) {
        // PayPal requiere payerId y paymentId en el request
        PagoResponseDTO response = pagoService.confirmarPago(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una preferencia de pago con MercadoPago.
     * 
     * Endpoint: POST /api/v1/pagos/mercadopago/crear-preferencia
     * 
     * @param request Datos del pago
     * @return ResponseEntity con initPoint para redirigir al usuario
     */
    @PostMapping("/mercadopago/crear-preferencia")
    public ResponseEntity<PagoResponseDTO> crearPreferenciaMercadoPago(@Valid @RequestBody PagoRequestDTO request) {
        request.setMetodoPago("mercadopago");
        PagoResponseDTO response = pagoService.crearPago(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Webhook de MercadoPago para recibir notificaciones de pago.
     * 
     * Endpoint: POST /api/v1/pagos/mercadopago/webhook
     * 
     * @param payload Payload del webhook de MercadoPago
     * @return ResponseEntity con estado 200 si se procesó correctamente
     */
    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Map<String, String>> webhookMercadoPago(@RequestBody Map<String, Object> payload) {
        try {
            String tipo = (String) payload.get("type");
            String dataId = (String) payload.get("data_id");

            if ("payment".equals(tipo)) {
                // Obtener el estado del pago desde la API de MercadoPago
                // Por ahora, usamos el data_id como preference_id
                String status = (String) payload.getOrDefault("action", "pending");
                mercadoPagoPaymentService.procesarWebhook(dataId, status);
            }

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Error al procesar webhook de MercadoPago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verifica el estado de un pago.
     * 
     * Endpoint: GET /api/v1/pagos/verificar/{pedidoId}
     * 
     * @param pedidoId ID del pedido
     * @return ResponseEntity con el estado actual del pago
     */
    @GetMapping("/verificar/{pedidoId}")
    public ResponseEntity<PagoResponseDTO> verificarEstadoPago(@PathVariable Integer pedidoId) {
        PagoResponseDTO response = pagoService.verificarEstadoPago(pedidoId);
        return ResponseEntity.ok(response);
    }
}