package com.ecoshop.service.checkout;

import com.ecoshop.dto.Checkout.CheckoutImpactoResponse;

/**
 * Interfaz del servicio para gestionar el proceso de checkout.
 * 
 * Este servicio se encarga de calcular el impacto ambiental completo
 * antes de finalizar una compra, combinando información del carrito,
 * impacto ambiental y eco-puntos.
 */
public interface CheckoutService {

    /**
     * Calcula el impacto ambiental completo del carrito antes de finalizar la compra.
     * 
     * Este método combina:
     * - Huella de carbono total del carrito
     * - CO₂ ahorrado vs productos convencionales
     * - Eco-puntos que se ganarán
     * - Agua ahorrada
     * - Equivalencias en términos comprensibles
     * 
     * @param usuarioId ID del usuario autenticado
     * @return CheckoutImpactoResponse con el impacto ambiental completo
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario o carrito no existen
     */
    CheckoutImpactoResponse calcularImpactoCheckout(Integer usuarioId);
}

