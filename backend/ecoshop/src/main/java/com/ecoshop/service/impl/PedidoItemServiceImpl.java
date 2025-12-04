package com.ecoshop.service.impl;

import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.PedidoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.PedidoItem.PedidoItemRequestDTO;
import com.ecoshop.dto.PedidoItem.PedidoItemResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.PedidoItemMapper;
import com.ecoshop.repository.PedidoItemRepository;
import com.ecoshop.repository.PedidoRepository;
import com.ecoshop.repository.ProductoRepository;
import com.ecoshop.service.PedidoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de items de pedido.
 * 
 * Esta clase contiene la lógica de negocio para gestionar items de pedido.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Manejar excepciones de negocio
 * - Gestionar relaciones (pedido, producto)
 * - Recalcular automáticamente el total del pedido cuando cambian los items
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (pedido existe, producto existe, cantidad válida, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Asignar relaciones (pedido, producto)
 * 4. Congelar el precio del producto (precioUnitario)
 * 5. Guardar en la base de datos
 * 6. Recalcular el total del pedido
 * 7. Convertir entidad a DTO de respuesta usando el mapper
 * 8. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class PedidoItemServiceImpl implements PedidoItemService {

    // Repositorio para acceder a la base de datos de items de pedido
    private final PedidoItemRepository pedidoItemRepository;
    
    // Repositorio para acceder a los pedidos
    private final PedidoRepository pedidoRepository;
    
    // Repositorio para acceder a los productos
    private final ProductoRepository productoRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final PedidoItemMapper pedidoItemMapper;

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
     * @throws ResourceNotFoundException si el pedido o el producto no existen
     * 
     * Ejemplo de uso:
     * POST /api/v1/pedido-items
     * Body: { "pedidoId": 1, "productoId": 5, "cantidad": 2 }
     */
    @Override
    @Transactional
    public PedidoItemResponseDTO addItem(PedidoItemRequestDTO dto) {
        // 1. Validar existencia del pedido
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + dto.getPedidoId()));

        // 2. Validar existencia del producto
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getProductoId()));

        // 3. Convertir DTO a entidad usando el mapper
        PedidoItem item = pedidoItemMapper.toEntity(dto);
        
        // 4. Asignar las relaciones con Pedido y Producto
        item.setPedido(pedido);
        item.setProducto(producto);
        
        // 5. Congelar el precio del producto (importante: si el precio cambia después, el pedido mantiene el precio original)
        item.setPrecioUnitario(producto.getPrecio());

        // 6. Guardar en la BD (JPA asigna el ID automáticamente)
        PedidoItem savedItem = pedidoItemRepository.save(item);

        // 7. Recalculamos el total del pedido padre
        recalcularTotalPedido(pedido.getPedidoId());

        // 8. Convertir entidad a DTO de respuesta usando el mapper
        return pedidoItemMapper.toResponse(savedItem);
    }

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
     * @throws ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedido-items/pedido/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<PedidoItemResponseDTO> getItemsByPedido(Integer pedidoId) {
        // Validamos que el pedido exista
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId);
        }
        
        // Buscamos items por pedido
        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedidoId);
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return items.stream()
                .map(pedidoItemMapper::toResponse) // Convierte cada PedidoItem a PedidoItemResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

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
     * @throws ResourceNotFoundException si el item no existe
     * @throws BadRequestException si la cantidad no es válida
     * 
     * Ejemplo de uso:
     * PUT /api/v1/pedido-items/1?cantidad=3
     */
    @Override
    @Transactional
    public PedidoItemResponseDTO updateCantidad(Integer itemId, Integer nuevaCantidad) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        PedidoItem item = pedidoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado con id: " + itemId));

        // 2. Validamos que la nueva cantidad sea válida
        if (nuevaCantidad == null || nuevaCantidad < 1) {
            throw new BadRequestException("La cantidad debe ser al menos 1");
        }

        // 3. Actualizamos la cantidad
        item.setCantidad(nuevaCantidad);

        // 4. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        PedidoItem savedItem = pedidoItemRepository.save(item);

        // 5. Recalculamos el total del pedido padre
        recalcularTotalPedido(item.getPedido().getPedidoId());

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return pedidoItemMapper.toResponse(savedItem);
    }

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
     * @throws ResourceNotFoundException si el item no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/pedido-items/1
     */
    @Override
    @Transactional
    public void removeItem(Integer itemId) {
        // Verificamos que el item exista y obtenemos el ID del pedido
        PedidoItem item = pedidoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado con id: " + itemId));
        
        Integer pedidoId = item.getPedido().getPedidoId();
        
        // Eliminamos el item de la BD
        pedidoItemRepository.deleteById(itemId);

        // Recalculamos el total del pedido padre
        recalcularTotalPedido(pedidoId);
    }

    /**
     * Recalcula el total de un pedido basándose en sus items.
     * 
     * Este método privado encapsula la lógica de recálculo del total del pedido.
     * Se llama automáticamente después de agregar, actualizar o eliminar items.
     * 
     * Proceso:
     * 1. Obtiene el pedido y todos sus items
     * 2. Calcula el subtotal de cada item (cantidad * precioUnitario)
     * 3. Suma todos los subtotales para obtener el total del pedido
     * 4. Actualiza el total del pedido en la BD
     * 
     * @param pedidoId Identificador del pedido a recalcular
     * @throws ResourceNotFoundException si el pedido no existe
     */
    private void recalcularTotalPedido(Integer pedidoId) {
        // Obtenemos el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        // Obtenemos todos los items del pedido
        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedidoId);

        // Calculamos el total sumando los subtotales de cada item
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        for (PedidoItem item : items) {
            BigDecimal subtotal = item.getPrecioUnitario()
                    .multiply(new BigDecimal(item.getCantidad()));
            nuevoTotal = nuevoTotal.add(subtotal);
        }

        // Actualizamos el total del pedido
        pedido.setTotal(nuevoTotal);
        pedidoRepository.save(pedido);
    }
}
