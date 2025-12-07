package com.ecoshop.controller.eco;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.EcoPuntos.CalculoEcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.EcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.HistorialEcoPuntosResponse;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.service.usuario.EcoPuntosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar eco-puntos.
 * 
 * Este controlador expone los endpoints HTTP para consultar y calcular eco-puntos.
 * Todos los endpoints están bajo las rutas base "/api/v1/eco-puntos" y "/api/v1/usuarios/{id}/eco-puntos".
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles:
 * ✅ POST /api/v1/eco-puntos/calcular?pedidoId=... - Calcular eco-puntos que se ganarían con un pedido (200 OK)
 * ✅ GET /api/v1/usuarios/{id}/eco-puntos - Obtener estado actual de eco-puntos de un usuario (200 OK)
 * ✅ GET /api/v1/usuarios/{id}/eco-puntos/historial - Obtener historial de eco-puntos ganados (200 OK)
 * 
 * Características:
 * - Cálculo de eco-puntos antes de finalizar la compra
 * - Consulta del estado actual de eco-puntos
 * - Historial completo de puntos ganados, estará vacio a menos que el pedido se encuentre como Entregado
 */
@RestController
@RequiredArgsConstructor
public class EcoPuntosController {

    private final EcoPuntosService ecoPuntosService;

    /**
     * Calcula los eco-puntos que se ganarían con un pedido antes de finalizarlo.
     * 
     * Endpoint: POST /api/v1/eco-puntos/calcular?pedidoId={id}
     * 
     * Este endpoint permite mostrar al usuario cuántos puntos ganará antes de confirmar la compra.
     * 
     * @param pedidoId ID del pedido para calcular puntos
     * @return ResponseEntity con el cálculo de puntos y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * POST /api/v1/eco-puntos/calcular?pedidoId=1
     */
    @PostMapping("/api/v1/eco-puntos/calcular")
    public ResponseEntity<CalculoEcoPuntosResponse> calcularPuntos(
            @RequestParam Integer pedidoId) {
        CalculoEcoPuntosResponse calculo = ecoPuntosService.calcularPuntosPedido(pedidoId);
        return ResponseEntity.ok(calculo);
    }

    /**
     * Obtiene el estado actual de eco-puntos de un usuario.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/eco-puntos
     * 
     * Validación de permisos: Los usuarios solo pueden acceder a sus propios eco-puntos,
     * excepto los administradores que pueden acceder a cualquier usuario.
     * 
     * @param id Identificador del usuario
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el estado de eco-puntos y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/eco-puntos
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping("/api/v1/usuarios/{id}/eco-puntos")
    public ResponseEntity<EcoPuntosResponse> getEcoPuntos(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios eco-puntos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder a los eco-puntos de este usuario");
        }
        
        EcoPuntosResponse ecoPuntos = ecoPuntosService.obtenerEcoPuntos(id);
        return ResponseEntity.ok(ecoPuntos);
    }

    /**
     * Obtiene el historial de eco-puntos ganados por un usuario.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/eco-puntos/historial
     * 
     * Validación de permisos: Los usuarios solo pueden acceder a su propio historial de eco-puntos,
     * excepto los administradores que pueden acceder a cualquier usuario.
     * 
     * @param id Identificador del usuario
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el historial de puntos y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/eco-puntos/historial
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping("/api/v1/usuarios/{id}/eco-puntos/historial")
    public ResponseEntity<HistorialEcoPuntosResponse> getHistorialEcoPuntos(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a su propio historial o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder al historial de eco-puntos de este usuario");
        }
        
        HistorialEcoPuntosResponse historial = ecoPuntosService.obtenerHistorialEcoPuntos(id);
        return ResponseEntity.ok(historial);
    }
}

