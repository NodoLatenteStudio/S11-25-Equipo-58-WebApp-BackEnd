package com.ecoshop.controller.checkout;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Checkout.CheckoutImpactoResponse;
import com.ecoshop.service.checkout.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar el proceso de checkout.
 * 
 * Este controlador expone los endpoints HTTP para el proceso de finalización de compra.
 * Todos los endpoints están bajo la ruta base "/api/v1/checkout".
 * 
 * Responsabilidades:
 * - Calcular el impacto ambiental completo antes de finalizar la compra
 * - Proporcionar información sobre eco-puntos que se ganarán
 * - Mostrar equivalencias en términos comprensibles
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles (todos requieren autenticación):
 * ✅ POST /api/v1/checkout/calcular-impacto - Calcular impacto ambiental del carrito antes del checkout (200 OK)
 * 
 * Características:
 * - UsuarioId automático: se obtiene del token JWT
 * - Cálculo completo de impacto ambiental del carrito
 * - Información de eco-puntos que se ganarán
 * - Equivalencias en términos comprensibles (árboles, duchas, etc.)
 * 
 * Nota: El usuario debe estar autenticado para acceder a estos endpoints por Clerk.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Calcula el impacto ambiental completo del carrito antes de finalizar la compra.
     * 
     * Endpoint: POST /api/v1/checkout/calcular-impacto
     * 
     * Este endpoint proporciona información completa sobre el impacto ambiental
     * que tendrá el pedido, incluyendo:
     * - Huella de carbono total
     * - CO₂ ahorrado vs productos convencionales
     * - Eco-puntos que se ganarán
     * - Agua ahorrada
     * - Equivalencias en términos comprensibles
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el impacto ambiental completo y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * POST /api/v1/checkout/calcular-impacto
     * Authorization: Bearer <token_jwt>
     * 
     * Ejemplo de respuesta:
     * {
     *   "huellaCarbono": 2.8,
     *   "co2Ahorrado": 1.1,
     *   "co2AhorradoEquivalente": "Equivalente a 0.1 árboles plantados",
     *   "ecoPuntosAGanar": 72,
     *   "aguaAhorrada": 150,
     *   "aguaAhorradaEquivalente": "Equivalente a 1 ducha de 5 minutos"
     * }
     */
    @PostMapping("/calcular-impacto")
    public ResponseEntity<CheckoutImpactoResponse> calcularImpacto(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CheckoutImpactoResponse impacto = checkoutService.calcularImpactoCheckout(usuario.getUsuarioId());
        return ResponseEntity.ok(impacto);
    }
}

