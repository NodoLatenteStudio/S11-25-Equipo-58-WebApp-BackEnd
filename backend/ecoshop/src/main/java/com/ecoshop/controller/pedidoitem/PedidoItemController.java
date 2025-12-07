package com.ecoshop.controller.pedidoitem;

import com.ecoshop.dto.PedidoItem.PedidoItemRequestDTO;
import com.ecoshop.dto.PedidoItem.PedidoItemResponseDTO;
import com.ecoshop.service.pedidoitem.PedidoItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar items de pedido.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre items de pedido.
 * Todos los endpoints están bajo la ruta base "/api/v1/pedido-items".
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
 * ✅ POST /api/v1/pedido-items - Agregar un item a un pedido (201 Created) - Requiere autenticación
 * ✅ GET /api/v1/pedido-items/pedido/{pedidoId} - Obtener todos los items de un pedido (200 OK) - Requiere autenticación
 * ✅ PUT /api/v1/pedido-items/{itemId} - Actualizar la cantidad de un item (200 OK) - Requiere autenticación
 * ✅ DELETE /api/v1/pedido-items/{itemId} - Eliminar un item de un pedido (204 No Content) - Requiere autenticación
 * 
 * Características:
 * - Recalculación automática del total del pedido al modificar items
 * - Precio del producto "congelado" al momento de agregarlo al pedido
 * - Validación de stock disponible antes de agregar items
 * - Actualización automática de inventario al agregar/eliminar items
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 * 
 * Nota importante:
 * Todas las operaciones que modifican items (agregar, actualizar cantidad, eliminar)
 * recalculan automáticamente el total del pedido padre.
 */
@RestController // Indica que esta clase es un controlador REST (combina @Controller + @ResponseBody)
@RequestMapping("/api/v1/pedido-items") // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
public class PedidoItemController {

    // Servicio que contiene la lógica de negocio
    private final PedidoItemService pedidoItemService;

    /**
     * Agrega un nuevo item a un pedido.
     * 
     * Endpoint: POST /api/v1/pedido-items
     * 
     * Este endpoint permite agregar un producto a un pedido existente.
     * El precio del producto se "congela" al momento de agregarlo, para que
     * si el precio cambia después, el pedido mantenga el precio original.
     * 
     * @param dto Datos del item a agregar (validados automáticamente)
     * @return ResponseEntity con el item creado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/pedido-items
     * Content-Type: application/json
     * Body: {
     *   "pedidoId": 1,
     *   "productoId": 5,
     *   "cantidad": 2
     * }
     */
    @PostMapping
    public ResponseEntity<PedidoItemResponseDTO> addItem(@Valid @RequestBody PedidoItemRequestDTO dto) {
        PedidoItemResponseDTO newItem = pedidoItemService.addItem(dto);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los items de un pedido específico.
     * 
     * Endpoint: GET /api/v1/pedido-items/pedido/{pedidoId}
     * 
     * Este endpoint es útil para mostrar el carrito de compras o la factura de un pedido.
     * Retorna todos los items del pedido con información del producto (nombre, imagen)
     * y el subtotal calculado de cada item.
     * 
     * @param pedidoId Identificador del pedido
     * @return ResponseEntity con la lista de items del pedido y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/pedido-items/pedido/1
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<PedidoItemResponseDTO>> getItemsByPedido(@PathVariable Integer pedidoId) {
        return ResponseEntity.ok(pedidoItemService.getItemsByPedido(pedidoId));
    }

    /**
     * Actualiza la cantidad de un item de pedido.
     * 
     * Endpoint: PUT /api/v1/pedido-items/{itemId}
     * 
     * Este endpoint permite cambiar la cantidad de un producto en el pedido
     * (ej: cambiar de 1 a 2 unidades). El total del pedido se recalcula automáticamente.
     * 
     * @param itemId Identificador del item a actualizar
     * @param cantidad Nueva cantidad del item (mínimo 1)
     * @return ResponseEntity con el item actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el item no existe
     * @throws com.ecoshop.exception.BadRequestException si la cantidad no es válida
     * 
     * Ejemplo de petición:
     * PUT /api/v1/pedido-items/1?cantidad=3
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<PedidoItemResponseDTO> updateCantidad(
            @PathVariable Integer itemId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(pedidoItemService.updateCantidad(itemId, cantidad));
    }

    /**
     * Elimina un item de un pedido.
     * 
     * Endpoint: DELETE /api/v1/pedido-items/{itemId}
     * 
     * Este endpoint permite eliminar un producto del pedido.
     * El total del pedido se recalcula automáticamente después de eliminar el item.
     * 
     * @param itemId Identificador del item a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el item no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/pedido-items/1
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer itemId) {
        pedidoItemService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
