package com.ecoshop.service;

import com.ecoshop.dto.Pedido.PedidoRequestDTO;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar pedidos.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre pedidos.
 * La implementación se encuentra en PedidoServiceImpl.
 * 
 * Responsabilidades:
 * - Crear nuevos pedidos
 * - Obtener pedidos por ID, todos los pedidos, o pedidos de un usuario
 * - Actualizar el estado de pedidos existentes
 * - Eliminar pedidos
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 */
public interface PedidoService {

    /**
     * Crea un nuevo pedido en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Convierte el DTO a entidad usando el mapper
     * 3. Asigna la relación con Usuario
     * 4. Inicializa el total en cero (se calculará a partir de los items)
     * 5. Guarda el pedido en la BD
     * 6. Convierte la entidad a DTO de respuesta usando el mapper
     * 7. Retorna el DTO con el ID asignado
     * 
     * @param dto PedidoRequestDTO con los datos del pedido a crear
     * @return PedidoResponseDTO con el pedido creado y su ID asignado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/pedidos
     * Body: { "usuarioId": 1, "direccionEnvio": "Av. Principal 123", ... }
     */
    PedidoResponseDTO createPedido(PedidoRequestDTO dto);

    /**
     * Busca un pedido por su ID.
     * 
     * Proceso:
     * 1. Busca el pedido en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, lo convierte a PedidoResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador del pedido
     * @return PedidoResponseDTO del pedido encontrado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos/1
     */
    PedidoResponseDTO getPedidoById(Integer id);

    /**
     * Obtiene todos los pedidos de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Pedido de la BD
     * 2. Convierte cada entidad a PedidoResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todos los pedidos convertidos a PedidoResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos
     */
    List<PedidoResponseDTO> getAllPedidos();

    /**
     * Obtiene todos los pedidos de un usuario específico.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Busca pedidos por ID de usuario en la BD
     * 3. Convierte cada entidad a PedidoResponseDTO usando el mapper
     * 4. Retorna la lista de DTOs
     * 
     * @param usuarioId Identificador del usuario
     * @return Lista de pedidos del usuario convertidos a PedidoResponseDTO
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos/usuario/1
     */
    List<PedidoResponseDTO> getPedidosByUsuario(Integer usuarioId);

    /**
     * Actualiza el estado de un pedido existente.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Valida que el nuevo estado sea válido
     * 3. Actualiza el estado del pedido
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID del pedido a actualizar
     * @param nuevoEstado Nuevo estado del pedido (pendiente_pago, procesando, enviado, entregado, cancelado)
     * @return PedidoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * @throws com.ecoshop.exception.BadRequestException si el estado no es válido
     * 
     * Ejemplo de uso:
     * PATCH /api/v1/pedidos/1/estado?estado=enviado
     */
    PedidoResponseDTO updateEstadoPedido(Integer id, String nuevoEstado);

    /**
     * Actualiza un pedido existente completamente.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Valida que el usuario exista (si se proporciona)
     * 3. Preserva el total y fecha de creación originales
     * 4. Actualiza los campos básicos usando el mapper
     * 5. Guarda los cambios en la BD
     * 6. Convierte la entidad a DTO de respuesta usando el mapper
     * 7. Retorna el DTO actualizado
     * 
     * @param id ID del pedido a actualizar
     * @param dto Nuevos datos del pedido
     * @return PedidoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido o usuario no existen
     * 
     * Ejemplo de uso:
     * PUT /api/v1/pedidos/1
     * Body: { "direccionEnvio": "Nueva dirección", "estado": "enviado", ... }
     */
    PedidoResponseDTO updatePedido(Integer id, PedidoRequestDTO dto);

    /**
     * Elimina un pedido de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Elimina el pedido de la BD
     * 
     * Nota: Si el pedido tiene PedidoItems asociados, la eliminación puede fallar
     * debido a restricciones de integridad referencial, a menos que se maneje
     * con CASCADE DELETE o eliminando primero los items.
     * 
     * @param id Identificador del pedido a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/pedidos/1
     */
    void deletePedido(Integer id);
}
