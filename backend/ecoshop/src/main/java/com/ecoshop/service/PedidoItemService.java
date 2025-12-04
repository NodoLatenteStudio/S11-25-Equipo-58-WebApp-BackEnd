package com.ecoshop.service;

import com.ecoshop.dto.PedidoItem.PedidoItemRequestDTO;
import com.ecoshop.dto.PedidoItem.PedidoItemResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar items de pedido.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre items de pedido.
 * La implementación se encuentra en PedidoItemServiceImpl.
 * 
 * Responsabilidades:
 * - Agregar items a un pedido
 * - Obtener items de un pedido
 * - Actualizar la cantidad de un item
 * - Eliminar items de un pedido
 * - Recalcular automáticamente el total del pedido cuando cambian los items
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 * 
 * Nota importante:
 * Todas las operaciones que modifican items (agregar, actualizar cantidad, eliminar)
 * recalculan automáticamente el total del pedido padre.
 */
public interface PedidoItemService {

    /**
     * Agrega un nuevo item a un pedido.
     * 
     * Proceso:
     * 1. Valida que el pedido y el producto existan
     * 2. Convierte el DTO a entidad usando el mapper
     * 3. Asigna las relaciones con Pedido y Producto
     * 4. Congela el precio del producto (precioUnitario)
     * 5. Guarda el item en la BD
     * 6. Recalcula el total del pedido
     * 7. Convierte la entidad a DTO de respuesta usando el mapper
     * 8. Retorna el DTO con el ID asignado
     * 
     * @param dto PedidoItemRequestDTO con los datos del item a agregar
     * @return PedidoItemResponseDTO con el item creado y su ID asignado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido o el producto no existen
     * 
     * Ejemplo de uso:
     * POST /api/v1/pedido-items
     * Body: { "pedidoId": 1, "productoId": 5, "cantidad": 2 }
     */
    PedidoItemResponseDTO addItem(PedidoItemRequestDTO dto);

    /**
     * Obtiene todos los items de un pedido específico.
     * 
     * Proceso:
     * 1. Valida que el pedido exista
     * 2. Busca items por ID de pedido en la BD
     * 3. Convierte cada entidad a PedidoItemResponseDTO usando el mapper
     * 4. Retorna la lista de DTOs
     * 
     * @param pedidoId Identificador del pedido
     * @return Lista de items del pedido convertidos a PedidoItemResponseDTO
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedido-items/pedido/1
     */
    List<PedidoItemResponseDTO> getItemsByPedido(Integer pedidoId);

    /**
     * Actualiza la cantidad de un item de pedido.
     * 
     * Proceso:
     * 1. Verifica que el item exista
     * 2. Valida que la nueva cantidad sea válida (mínimo 1)
     * 3. Actualiza la cantidad del item
     * 4. Guarda los cambios en la BD
     * 5. Recalcula el total del pedido
     * 6. Convierte la entidad a DTO de respuesta usando el mapper
     * 7. Retorna el DTO actualizado
     * 
     * @param itemId ID del item a actualizar
     * @param nuevaCantidad Nueva cantidad del item (mínimo 1)
     * @return PedidoItemResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el item no existe
     * @throws com.ecoshop.exception.BadRequestException si la cantidad no es válida
     * 
     * Ejemplo de uso:
     * PUT /api/v1/pedido-items/1?cantidad=3
     */
    PedidoItemResponseDTO updateCantidad(Integer itemId, Integer nuevaCantidad);

    /**
     * Elimina un item de un pedido.
     * 
     * Proceso:
     * 1. Verifica que el item exista
     * 2. Obtiene el ID del pedido antes de eliminar
     * 3. Elimina el item de la BD
     * 4. Recalcula el total del pedido
     * 
     * @param itemId Identificador del item a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si el item no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/pedido-items/1
     */
    void removeItem(Integer itemId);
}
