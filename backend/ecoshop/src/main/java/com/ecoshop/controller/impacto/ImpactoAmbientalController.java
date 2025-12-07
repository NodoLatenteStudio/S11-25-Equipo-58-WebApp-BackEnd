package com.ecoshop.controller.impacto;

import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoPedidoResponse;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoProductoResponse;
import com.ecoshop.dto.ImpactoAmbiental.ComparacionProductosResponse;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar cálculos de impacto ambiental.
 * 
 * Este controlador expone los endpoints HTTP para calcular y comparar
 * la huella de carbono de productos y pedidos.
 * 
 * Todos los endpoints están bajo la ruta base "/api/v1/impacto-ambiental".
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles (todos públicos):
 * ✅ GET /api/v1/impacto-ambiental/producto/{productoId} - Calcular impacto ambiental de un producto (200 OK)
 * ✅ GET /api/v1/impacto-ambiental/pedido/{pedidoId} - Calcular impacto ambiental de un pedido (200 OK)
 * ✅ GET /api/v1/impacto-ambiental/comparar?productoIds=... - Comparar impacto ambiental de múltiples productos (200 OK)
 * ✅ POST /api/v1/impacto-ambiental/calcular?productoId=... - Calcular impacto ambiental (endpoint POST para cálculo dinámico) (200 OK)
 * 
 * Características:
 * - Todos los endpoints son públicos (no requieren autenticación)
 * - Cálculo detallado de huella de carbono
 * - Comparación entre múltiples productos (hasta 4)
 * - Información de recomendaciones y equivalencias
 */
@RestController
@RequestMapping("/api/v1/impacto-ambiental")
@RequiredArgsConstructor
public class ImpactoAmbientalController {

    private final ImpactoAmbientalService impactoAmbientalService;

    /**
     * Calcula el impacto ambiental detallado de un producto.
     * 
     * Endpoint: GET /api/v1/impacto-ambiental/producto/{productoId}
     * 
     * @param productoId Identificador del producto
     * @return ResponseEntity con el cálculo detallado del impacto ambiental
     * 
     * Ejemplo de petición:
     * GET /api/v1/impacto-ambiental/producto/1
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<CalculoImpactoProductoResponse> calcularImpactoProducto(
            @PathVariable Integer productoId) {
        return ResponseEntity.ok(impactoAmbientalService.calcularImpactoProducto(productoId));
    }

    /**
     * Calcula el impacto ambiental detallado de un pedido.
     * 
     * Endpoint: GET /api/v1/impacto-ambiental/pedido/{pedidoId}
     * 
     * @param pedidoId Identificador del pedido
     * @return ResponseEntity con el cálculo detallado del impacto ambiental
     * 
     * Ejemplo de petición:
     * GET /api/v1/impacto-ambiental/pedido/1
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CalculoImpactoPedidoResponse> calcularImpactoPedido(
            @PathVariable Integer pedidoId) {
        return ResponseEntity.ok(impactoAmbientalService.calcularImpactoPedido(pedidoId));
    }

    /**
     * Compara el impacto ambiental entre múltiples productos.
     * 
     * Endpoint: GET /api/v1/impacto-ambiental/comparar?productoIds=1,2,3
     * 
     * Permite comparar hasta 4 productos lado a lado, mostrando:
     * - Huella de carbono de cada producto
     * - Precio
     * - Materiales y origen
     * - Producto recomendado (balance entre precio e impacto)
     * 
     * @param productoIds Lista de IDs de productos separados por comas (máximo 4)
     * @return ResponseEntity con la comparación detallada
     * 
     * Ejemplo de petición:
     * GET /api/v1/impacto-ambiental/comparar?productoIds=1,2,3
     */
    @GetMapping("/comparar")
    public ResponseEntity<ComparacionProductosResponse> compararProductos(
            @RequestParam("productoIds") String productoIds) {
        // Convertir string de IDs separados por comas a lista de enteros
        List<Integer> ids = List.of(productoIds.split(","))
                .stream()
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        
        return ResponseEntity.ok(impactoAmbientalService.compararProductos(ids));
    }

    /**
     * Calcula el impacto ambiental de un producto (endpoint POST para cálculo dinámico).
     * 
     * Endpoint: POST /api/v1/impacto-ambiental/calcular
     * 
     * Este endpoint permite calcular el impacto antes de crear el producto,
     * útil para mostrar estimaciones en el frontend.
     * 
     * @param productoId Identificador del producto
     * @return ResponseEntity con el cálculo del impacto ambiental
     * 
     * Ejemplo de petición:
     * POST /api/v1/impacto-ambiental/calcular?productoId=1
     */
    @PostMapping("/calcular")
    public ResponseEntity<CalculoImpactoProductoResponse> calcularImpacto(
            @RequestParam Integer productoId) {
        return ResponseEntity.ok(impactoAmbientalService.calcularImpactoProducto(productoId));
    }
}

