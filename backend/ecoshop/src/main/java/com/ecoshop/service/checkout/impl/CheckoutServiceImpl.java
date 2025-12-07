package com.ecoshop.service.checkout.impl;

import com.ecoshop.domain.Carrito;
import com.ecoshop.domain.CarritoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.Checkout.CheckoutImpactoResponse;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.carrito.CarritoItemRepository;
import com.ecoshop.repository.carrito.CarritoRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.carrito.CarritoService;
import com.ecoshop.service.checkout.CheckoutService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementación del servicio para gestionar el proceso de checkout.
 * 
 * Este servicio combina información del carrito, impacto ambiental y eco-puntos
 * para proporcionar un cálculo completo del impacto antes de finalizar la compra.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutServiceImpl implements CheckoutService {

    private final CarritoService carritoService;
    private final ImpactoAmbientalService impactoAmbientalService;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final UsuarioRepository usuarioRepository;

    // Factores para cálculo de eco-puntos (mismo que en EcoPuntosServiceImpl)
    private static final double PUNTOS_POR_PESO = 0.1; // 0.1 puntos por cada peso gastado
    private static final int BONUS_BAJO_IMPACTO = 20; // Puntos por producto con bajo impacto
    private static final int BONUS_RECICLABLE = 10; // Puntos por producto 100% reciclable
    private static final int BONUS_CERTIFICACION = 5; // Puntos por certificación ambiental

    @Override
    public CheckoutImpactoResponse calcularImpactoCheckout(Integer usuarioId) {
        // Validar que el usuario existe
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Obtener el carrito
        var carritoResponse = carritoService.obtenerCarrito(usuarioId);
        
        if (carritoResponse.getItems() == null || carritoResponse.getItems().isEmpty()) {
            return CheckoutImpactoResponse.builder()
                    .huellaCarbono(BigDecimal.ZERO)
                    .co2Ahorrado(BigDecimal.ZERO)
                    .co2AhorradoEquivalente("El carrito está vacío")
                    .ecoPuntosAGanar(0)
                    .aguaAhorrada(0)
                    .aguaAhorradaEquivalente("El carrito está vacío")
                    .build();
        }

        // Obtener el carrito de la BD para acceder a los items
        Carrito carrito = carritoRepository.findByUsuario_UsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado para el usuario: " + usuarioId));

        List<CarritoItem> items = carritoItemRepository.findByCarrito_CarritoId(carrito.getCarritoId());

        // Calcular huella de carbono y CO₂ ahorrado
        BigDecimal huellaCarbonoTotal = BigDecimal.ZERO;
        BigDecimal co2Ahorrado = BigDecimal.ZERO;
        Integer aguaAhorrada = 0;

        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();

            // Calcular huella de carbono del producto
            BigDecimal huellaProducto = impactoAmbientalService.calcularHuellaCarbonoProducto(producto);
            huellaCarbonoTotal = huellaCarbonoTotal.add(huellaProducto.multiply(new BigDecimal(cantidad)));

            // Calcular CO₂ ahorrado
            if (producto.getCo2AhorradoVsConvencional() != null) {
                co2Ahorrado = co2Ahorrado.add(
                        producto.getCo2AhorradoVsConvencional().multiply(new BigDecimal(cantidad))
                );
            }

            // Calcular agua ahorrada
            if (producto.getConsumoAgua() != null) {
                aguaAhorrada += producto.getConsumoAgua() * cantidad;
            }
        }

        // Calcular eco-puntos que se ganarán
        Integer ecoPuntosAGanar = calcularEcoPuntosCarrito(carrito, items);

        // Calcular equivalencias
        String co2Equivalente = calcularEquivalenteCO2(co2Ahorrado);
        String aguaEquivalente = calcularEquivalenteAgua(aguaAhorrada);

        return CheckoutImpactoResponse.builder()
                .huellaCarbono(huellaCarbonoTotal.setScale(2, RoundingMode.HALF_UP))
                .co2Ahorrado(co2Ahorrado.setScale(2, RoundingMode.HALF_UP))
                .co2AhorradoEquivalente(co2Equivalente)
                .ecoPuntosAGanar(ecoPuntosAGanar)
                .aguaAhorrada(aguaAhorrada)
                .aguaAhorradaEquivalente(aguaEquivalente)
                .build();
    }

    /**
     * Calcula los eco-puntos que se ganarán con el carrito actual.
     */
    private Integer calcularEcoPuntosCarrito(Carrito carrito, List<CarritoItem> items) {
        // Puntos base: 0.1 puntos por cada peso gastado
        int puntosBase = (int) (carrito.getTotal().doubleValue() * PUNTOS_POR_PESO);

        // Puntos bonus por productos sostenibles
        int puntosBonus = 0;

        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();
            int puntosItem = 0;

            // Bonus por bajo impacto (eco-badge)
            if (producto.getEcoBadge() != null && "bajo_impacto".equalsIgnoreCase(producto.getEcoBadge())) {
                puntosItem += BONUS_BAJO_IMPACTO * cantidad;
            }

            // Bonus por reciclable
            if (producto.getPorcentajeReciclable() != null) {
                if (producto.getPorcentajeReciclable() == 100) {
                    puntosItem += BONUS_RECICLABLE * cantidad;
                } else if (producto.getPorcentajeReciclable() >= 50) {
                    puntosItem += (BONUS_RECICLABLE / 2) * cantidad;
                }
            }

            // Bonus por certificaciones
            if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
                puntosItem += producto.getCertificaciones().size() * BONUS_CERTIFICACION * cantidad;
            }

            puntosBonus += puntosItem;
        }

        return puntosBase + puntosBonus;
    }

    /**
     * Calcula el equivalente de CO₂ ahorrado en términos comprensibles.
     */
    private String calcularEquivalenteCO2(BigDecimal kgCO2) {
        if (kgCO2 == null || kgCO2.compareTo(BigDecimal.ZERO) == 0) {
            return "Sin CO₂ ahorrado";
        }

        // Un árbol absorbe aproximadamente 22 kg de CO₂ al año
        BigDecimal arboles = kgCO2.divide(new BigDecimal("22"), 1, RoundingMode.HALF_UP);
        
        if (arboles.compareTo(new BigDecimal("1")) < 0) {
            return "Equivalente a menos de 1 árbol plantado";
        } else if (arboles.compareTo(new BigDecimal("10")) < 0) {
            return String.format("Equivalente a %.1f árboles plantados", arboles.doubleValue());
        } else {
            return String.format("Equivalente a %.0f árboles plantados", arboles.doubleValue());
        }
    }

    /**
     * Calcula el equivalente de agua ahorrada en términos comprensibles.
     */
    private String calcularEquivalenteAgua(Integer litros) {
        if (litros == null || litros == 0) {
            return "Sin agua ahorrada";
        }

        // Una ducha promedio usa ~150 litros
        int duchas = litros / 150;
        if (duchas > 0) {
            return String.format("Equivalente a %d ducha%s de 5 minutos", duchas, duchas > 1 ? "s" : "");
        }

        // Botellas de agua de 500ml
        int botellas = litros * 2;
        return String.format("Equivalente a %d botella%s de agua (500ml)", botellas, botellas > 1 ? "s" : "");
    }
}

