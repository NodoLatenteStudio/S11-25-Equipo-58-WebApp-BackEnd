package com.ecoshop.controller.pedido;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoPedidoResponse;
import com.ecoshop.dto.Pedido.PedidoRequestDTO;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.pedido.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar pedidos.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre pedidos.
 * Todos los endpoints están bajo la ruta base "/api/v1/pedidos".
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles:
 * ✅ POST /api/v1/pedidos - Crear un nuevo pedido (201 Created) - Requiere autenticación
 * ✅ GET /api/v1/pedidos - Obtener todos los pedidos (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/pedidos/{id} - Obtener un pedido por ID (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/pedidos/usuario/{usuarioId} - Obtener pedidos de un usuario (200 OK) - Requiere autenticación
 * ✅ PUT /api/v1/pedidos/{id} - Actualizar un pedido completamente (200 OK) - Requiere autenticación
 * ✅ PATCH /api/v1/pedidos/{id}/estado - Actualizar el estado de un pedido (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/pedidos/{id}/impacto-ambiental - Obtener impacto ambiental del pedido (200 OK) - Requiere autenticación
 * ✅ DELETE /api/v1/pedidos/{id} - Eliminar un pedido (204 No Content) - Requiere autenticación
 * 
 * Características:
 * - El usuarioId se obtiene automáticamente del token JWT (no se envía en el body)
 * - Validación automática de estados de pedido
 * - Cálculo automático de impacto ambiental
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 */
@RestController // Indica que esta clase es un controlador REST (combina @Controller + @ResponseBody)
@RequestMapping("/api/v1/pedidos") // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
public class PedidoController {

    // Servicio que contiene la lógica de negocio
    private final PedidoService pedidoService;
    
    // Servicio para calcular impacto ambiental
    private final ImpactoAmbientalService impactoAmbientalService;

    /**
     * Crea un nuevo pedido.
     * 
     * Endpoint: POST /api/v1/pedidos
     * 
     * El usuarioId se obtiene automáticamente del usuario autenticado (token JWT).
     * No es necesario incluir usuarioId en el body.
     * 
     * @param dto Datos del pedido a crear (validados automáticamente)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el pedido creado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/pedidos
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "direccionEnvio": "Av. Principal 123, Santiago, Chile",
     *   "estado": "pendiente_pago",
     *   "metodoPago": "stripe"
     * }
     */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> createPedido(
            @Valid @RequestBody PedidoRequestDTO dto,
            Authentication authentication) {
        // Obtener el usuario autenticado del SecurityContext
        Usuario usuario = (Usuario) authentication.getPrincipal();
        // Establecer el usuarioId del usuario autenticado
        dto.setUsuarioId(usuario.getUsuarioId());
        
        PedidoResponseDTO newPedido = pedidoService.createPedido(dto);
        return new ResponseEntity<>(newPedido, HttpStatus.CREATED);
    }

    /**
     * Obtiene un pedido por su ID.
     * 
     * Endpoint: GET /api/v1/pedidos/{id}
     * 
     * @param id Identificador del pedido
     * @return ResponseEntity con el pedido encontrado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/pedidos/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> getPedidoById(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.getPedidoById(id));
    }

    /**
     * Obtiene todos los pedidos.
     * 
     * Endpoint: GET /api/v1/pedidos
     * 
     * @return ResponseEntity con la lista de pedidos y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/pedidos
     */
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.getAllPedidos());
    }

    /**
     * Obtiene todos los pedidos de un usuario específico.
     * 
     * Endpoint: GET /api/v1/pedidos/usuario/{usuarioId}
     * 
     * Este endpoint es útil para mostrar el historial de pedidos de un usuario.
     * 
     * @param usuarioId Identificador del usuario
     * @return ResponseEntity con la lista de pedidos del usuario y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/pedidos/usuario/1
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosByUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(pedidoService.getPedidosByUsuario(usuarioId));
    }

    /**
     * Actualiza un pedido existente completamente.
     * 
     * Endpoint: PUT /api/v1/pedidos/{id}
     * 
     * El usuarioId se obtiene automáticamente del usuario autenticado (token JWT).
     * Si se proporciona usuarioId en el body, será sobrescrito por el del usuario autenticado.
     * 
     * @param id Identificador del pedido a actualizar
     * @param dto Nuevos datos del pedido (validados automáticamente)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el pedido actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/pedidos/1
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "direccionEnvio": "Nueva dirección de envío",
     *   "estado": "enviado",
     *   "metodoPago": "tarjeta_credito"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> updatePedido(
            @PathVariable Integer id,
            @Valid @RequestBody PedidoRequestDTO dto,
            Authentication authentication) {
        // Obtener el usuario autenticado del SecurityContext
        Usuario usuario = (Usuario) authentication.getPrincipal();
        // Establecer el usuarioId del usuario autenticado
        dto.setUsuarioId(usuario.getUsuarioId());
        
        return ResponseEntity.ok(pedidoService.updatePedido(id, dto));
    }

    /**
     * Actualiza el estado de un pedido existente.
     * 
     * Endpoint: PATCH /api/v1/pedidos/{id}/estado
     * 
     * Este endpoint permite cambiar el estado de un pedido (ej: de 'procesando' a 'enviado').
     * Se usa PATCH porque solo actualiza un campo específico (el estado).
     * 
     * @param id Identificador del pedido a actualizar
     * @param estado Nuevo estado del pedido (pendiente_pago, procesando, enviado, entregado, cancelado)
     * @return ResponseEntity con el pedido actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * @throws com.ecoshop.exception.BadRequestException si el estado no es válido
     * 
     * Ejemplo de petición:
     * PATCH /api/v1/pedidos/1/estado?estado=enviado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> updateEstado(
            @PathVariable Integer id, 
            @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.updateEstadoPedido(id, estado));
    }

    /**
     * Obtiene el detalle del impacto ambiental de un pedido.
     * 
     * Endpoint: GET /api/v1/pedidos/{id}/impacto-ambiental
     * 
     * Este endpoint proporciona un desglose completo del impacto ambiental del pedido,
     * incluyendo emisiones por producto, transporte y entrega, así como el CO₂ ahorrado.
     * 
     * @param id Identificador del pedido
     * @return ResponseEntity con el detalle del impacto ambiental y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/pedidos/1/impacto-ambiental
     */
    @GetMapping("/{id}/impacto-ambiental")
    public ResponseEntity<CalculoImpactoPedidoResponse> getImpactoAmbiental(@PathVariable Integer id) {
        CalculoImpactoPedidoResponse impacto = impactoAmbientalService.calcularImpactoPedido(id);
        return ResponseEntity.ok(impacto);
    }

    /**
     * Elimina un pedido.
     * 
     * Endpoint: DELETE /api/v1/pedidos/{id}
     * 
     * @param id Identificador del pedido a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/pedidos/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Integer id) {
        pedidoService.deletePedido(id);
        return ResponseEntity.noContent().build();
    }
}
