package com.ecoshop.controller;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Pedido.PedidoRequestDTO;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;
import com.ecoshop.service.PedidoService;
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
 * Convenciones REST:
 * - POST /api/v1/pedidos: Crear un nuevo pedido (201 Created)
 * - GET /api/v1/pedidos/{id}: Obtener un pedido por ID (200 OK)
 * - GET /api/v1/pedidos: Obtener todos los pedidos (200 OK)
 * - GET /api/v1/pedidos/usuario/{usuarioId}: Obtener pedidos de un usuario (200 OK)
 * - PATCH /api/v1/pedidos/{id}/estado: Actualizar el estado de un pedido (200 OK)
 * - DELETE /api/v1/pedidos/{id}: Eliminar un pedido (204 No Content)
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

    /**
     * Crea un nuevo pedido.
     * 
     * Endpoint: POST /api/v1/pedidos
     * 
     * @param dto Datos del pedido a crear (validados automáticamente)
     * @return ResponseEntity con el pedido creado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/pedidos
     * Content-Type: application/json
     * Body: {
     *   "usuarioId": 1,
     *   "direccionEnvio": "Av. Principal 123, Santiago, Chile",
     *   "estado": "pendiente_pago",
     *   "metodoPago": "tarjeta_credito"
     * }
     */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> createPedido(@Valid @RequestBody PedidoRequestDTO dto) {
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
