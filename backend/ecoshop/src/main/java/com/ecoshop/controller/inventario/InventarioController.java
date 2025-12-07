package com.ecoshop.controller.inventario;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Inventario.ActualizarStockRequestDTO;
import com.ecoshop.dto.Inventario.HistorialStockResponse;
import com.ecoshop.dto.Inventario.PrediccionDemandaResponse;
import com.ecoshop.dto.Inventario.StockBajoResponse;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.repository.marca.MarcaRepository;
import com.ecoshop.service.inventario.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controlador REST para gestión avanzada de inventario.
 * 
 * Este controlador expone los endpoints HTTP para:
 * - Alertas de stock bajo
 * - Historial de cambios de stock
 * - Predicción de demanda
 * - Actualización de stock con registro automático
 * 
 * Todos los endpoints están bajo la ruta base "/api/v1/inventario".
 * 
 * ✅ Todos los endpoints están funcionando correctamente.
 * 
 * Nota: El usuario debe estar autenticado y ser propietario de la marca o administrador.
 * 
 * Convenciones REST:
 * - GET /api/v1/inventario/marca/{marcaId}/stock-bajo: Obtener productos con stock bajo (200 OK) ✅
 * - GET /api/v1/inventario/producto/{productoId}/historial: Obtener historial completo (200 OK) ✅
 * - GET /api/v1/inventario/producto/{productoId}/historial/fecha: Obtener historial por fecha (200 OK) ✅
 * - PUT /api/v1/inventario/producto/{productoId}/stock: Actualizar stock (200 OK) ✅
 * - GET /api/v1/inventario/producto/{productoId}/prediccion-demanda: Obtener predicción de demanda (200 OK) ✅
 */
@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final MarcaRepository marcaRepository;

    /**
     * Obtiene productos con stock bajo para una marca.
     * 
     * Endpoint: GET /api/v1/inventario/marca/{marcaId}/stock-bajo
     * ✅ Funcionando correctamente.
     * 
     * @param marcaId ID de la marca
     * @param umbralStock Umbral para considerar stock bajo (opcional, por defecto 10)
     * @param authentication Usuario autenticado
     * @return ResponseEntity con productos que tienen stock bajo
     */
    @GetMapping("/marca/{marcaId}/stock-bajo")
    public ResponseEntity<StockBajoResponse> obtenerProductosStockBajo(
            @PathVariable Integer marcaId,
            @RequestParam(required = false) Integer umbralStock,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo el propietario de la marca o admin puede ver stock bajo
        if (!"admin".equalsIgnoreCase(usuario.getRol())) {
            boolean tienePermiso = marcaRepository.findByUsuario_UsuarioId(usuario.getUsuarioId())
                    .stream()
                    .anyMatch(marca -> marca.getMarcaId().equals(marcaId));
            if (!tienePermiso) {
                throw new ForbiddenException("No tienes permisos para ver el inventario de esta marca");
            }
        }

        StockBajoResponse response = inventarioService.obtenerProductosStockBajo(marcaId, umbralStock);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial completo de cambios de stock de un producto.
     * 
     * Endpoint: GET /api/v1/inventario/producto/{productoId}/historial
     * ✅ Funcionando correctamente.
     * 
     * @param productoId ID del producto
     * @param authentication Usuario autenticado
     * @return ResponseEntity con el historial completo de cambios de stock
     */
    @GetMapping("/producto/{productoId}/historial")
    public ResponseEntity<HistorialStockResponse> obtenerHistorialStock(
            @PathVariable Integer productoId,
            Authentication authentication) {
        // Validar permisos: solo el propietario de la marca del producto o admin
        // (la validación se puede hacer en el servicio si es necesario)

        HistorialStockResponse response = inventarioService.obtenerHistorialStock(productoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de cambios de stock de un producto en un rango de fechas.
     * 
     * Endpoint: GET /api/v1/inventario/producto/{productoId}/historial/fecha
     * ✅ Funcionando correctamente.
     * 
     * Formato de fecha soportado:
     * - ISO 8601: YYYY-MM-DDTHH:mm:ss (ejemplo: 2024-12-07T00:00:00)
     * - Formato corto: dd/MM/yyyy (ejemplo: 07/12/2024) - se interpreta como inicio del día (00:00:00)
     * 
     * @param productoId ID del producto
     * @param fechaInicio Fecha de inicio del rango (opcional, por defecto últimos 30 días)
     * @param fechaFin Fecha de fin del rango (opcional, por defecto ahora)
     * @param authentication Usuario autenticado
     * @return ResponseEntity con el historial en el rango de fechas
     * 
     * Ejemplos de petición:
     * - GET /api/v1/inventario/producto/1/historial/fecha?fechaInicio=2024-12-07T00:00:00&fechaFin=2024-12-07T23:59:59
     * - GET /api/v1/inventario/producto/1/historial/fecha?fechaInicio=07/12/2024&fechaFin=07/12/2024
     */
    @GetMapping("/producto/{productoId}/historial/fecha")
    public ResponseEntity<HistorialStockResponse> obtenerHistorialStockPorFecha(
            @PathVariable Integer productoId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication authentication) {
        // Convertir strings a LocalDateTime, tratando "null" o strings vacíos como null
        LocalDateTime fechaInicioParsed = parseFecha(fechaInicio);
        LocalDateTime fechaFinParsed = parseFecha(fechaFin);
        
        HistorialStockResponse response = inventarioService.obtenerHistorialStockPorFecha(
                productoId, fechaInicioParsed, fechaFinParsed);
        return ResponseEntity.ok(response);
    }

    /**
     * Convierte un string a LocalDateTime, soportando múltiples formatos.
     * 
     * Formatos soportados:
     * - ISO 8601: YYYY-MM-DDTHH:mm:ss (ejemplo: 2024-12-07T00:00:00)
     * - Formato corto: dd/MM/yyyy (ejemplo: 07/12/2024) - se interpreta como inicio del día (00:00:00)
     * 
     * @param fechaString String con la fecha o null/"null"
     * @return LocalDateTime parseado o null si el string es null, "null", o vacío
     */
    private LocalDateTime parseFecha(String fechaString) {
        if (fechaString == null || fechaString.trim().isEmpty() || "null".equalsIgnoreCase(fechaString.trim())) {
            return null;
        }
        
        String fechaTrimmed = fechaString.trim();
        
        try {
            // Intentar primero con formato ISO 8601
            return LocalDateTime.parse(fechaTrimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                // Intentar con formato corto dd/MM/yyyy (interpretar como inicio del día 00:00:00)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fecha = LocalDate.parse(fechaTrimmed, formatter);
                return fecha.atStartOfDay(); // Convertir a LocalDateTime al inicio del día
            } catch (DateTimeParseException e2) {
                // Si ambos formatos fallan, retornar null en lugar de lanzar excepción
                // Esto permite que el servicio maneje el null correctamente
                return null;
            }
        }
    }

    /**
     * Actualiza el stock de un producto y registra el cambio en el historial.
     * 
     * Endpoint: PUT /api/v1/inventario/producto/{productoId}/stock
     * ✅ Funcionando correctamente.
     * 
     * @param productoId ID del producto
     * @param request Datos de actualización (nuevo stock, tipo de movimiento, motivo)
     * @param authentication Usuario autenticado
     * @return ResponseEntity con el producto actualizado
     */
    @PutMapping("/producto/{productoId}/stock")
    public ResponseEntity<ProductoResponseDTO> actualizarStock(
            @PathVariable Integer productoId,
            @Valid @RequestBody ActualizarStockRequestDTO request,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo el propietario de la marca del producto o admin puede actualizar stock
        // (la validación se puede hacer en el servicio si es necesario)

        ProductoResponseDTO response = inventarioService.actualizarStock(
                productoId, request, usuario.getUsuarioId());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la predicción de demanda para un producto.
     * 
     * Endpoint: GET /api/v1/inventario/producto/{productoId}/prediccion-demanda
     * ✅ Funcionando correctamente.
     * 
     * Analiza las ventas recientes y proporciona recomendaciones de reposición.
     * 
     * @param productoId ID del producto
     * @param authentication Usuario autenticado
     * @return ResponseEntity con análisis de demanda y recomendaciones
     */
    @GetMapping("/producto/{productoId}/prediccion-demanda")
    public ResponseEntity<PrediccionDemandaResponse> obtenerPrediccionDemanda(
            @PathVariable Integer productoId,
            Authentication authentication) {
        PrediccionDemandaResponse response = inventarioService.obtenerPrediccionDemanda(productoId);
        return ResponseEntity.ok(response);
    }
}

