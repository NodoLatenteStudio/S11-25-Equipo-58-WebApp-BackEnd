package com.ecoshop.mapper;

import com.ecoshop.domain.PedidoItem;
import com.ecoshop.dto.PedidoItem.PedidoItemRequestDTO;
import com.ecoshop.dto.PedidoItem.PedidoItemResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper para convertir entre entidades PedidoItem y DTOs.
 * 
 * Esta clase se encarga de transformar objetos entre la capa de dominio (entidades JPA)
 * y la capa de presentación (DTOs). Esto permite:
 * - Separar la estructura de la BD de la estructura de la API
 * - Controlar qué datos se exponen en la API
 * - Facilitar cambios en la estructura sin afectar otras capas
 * 
 * Patrón de diseño: Mapper/Converter
 * 
 * Notas importantes:
 * - Los métodos toEntity() y toResponse() crean nuevas instancias
 * - El método updateEntityFromDto() actualiza una entidad existente
 * - Las relaciones con Pedido y Producto se manejan en el servicio, no en el mapper
 * - El precio_unitario se obtiene del producto al momento de agregarlo al pedido
 * - El subtotal se calcula automáticamente en el DTO de respuesta
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class PedidoItemMapper {

    /**
     * Convierte un PedidoItemRequestDTO a una entidad PedidoItem.
     * 
     * Este método se usa al crear un nuevo item de pedido. La entidad resultante
     * no tiene ID asignado (se asignará al guardar en la BD) y no tiene
     * las relaciones con Pedido y Producto establecidas (deben establecerse en el servicio).
     * 
     * IMPORTANTE: Este método NO establece:
     * - pedidoItemId: Se asigna automáticamente al guardar en la BD
     * - pedido: Debe establecerse en el servicio usando PedidoItemRequestDTO.getPedidoId()
     * - producto: Debe establecerse en el servicio usando PedidoItemRequestDTO.getProductoId()
     * - precioUnitario: Se obtiene del producto en el servicio al momento de agregarlo
     * 
     * @param dto DTO con los datos del item a crear
     * @return Entidad PedidoItem sin ID ni relaciones establecidas
     * 
     * Ejemplo de uso:
     * PedidoItemRequestDTO dto = new PedidoItemRequestDTO(...);
     * PedidoItem item = pedidoItemMapper.toEntity(dto);
     * item.setPedido(pedido); // Establecer relación en el servicio
     * item.setProducto(producto); // Establecer relación en el servicio
     * item.setPrecioUnitario(producto.getPrecio()); // Congelar precio
     * pedidoItemRepository.save(item);
     */
    public PedidoItem toEntity(PedidoItemRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return PedidoItem.builder()
                .cantidad(dto.getCantidad())
                // Nota: pedido, producto y precioUnitario se establecen en el servicio
                .build();
    }

    /**
     * Convierte una entidad PedidoItem a un PedidoItemResponseDTO.
     * 
     * Este método se usa al retornar datos de un item de pedido al cliente.
     * Extrae los IDs de las relaciones con Pedido y Producto, y también incluye
     * datos útiles del producto (nombre, imagen) y calcula el subtotal.
     * 
     * IMPORTANTE: Si las relaciones con Pedido y Producto no están cargadas (lazy loading),
     * se debe asegurar que estén cargadas antes de llamar a este método, o
     * se lanzará una excepción LazyInitializationException.
     * 
     * @param item Entidad PedidoItem a convertir
     * @return DTO de respuesta con los datos del item
     * 
     * Ejemplo de uso:
     * PedidoItem item = pedidoItemRepository.findById(id).orElseThrow(...);
     * PedidoItemResponseDTO dto = pedidoItemMapper.toResponse(item);
     * return dto;
     */
    public PedidoItemResponseDTO toResponse(PedidoItem item) {
        if (item == null) {
            return null;
        }

        // Calculamos el subtotal (cantidad * precioUnitario)
        BigDecimal subtotal = item.getPrecioUnitario()
                .multiply(new BigDecimal(item.getCantidad()));

        return PedidoItemResponseDTO.builder()
                .pedidoItemId(item.getPedidoItemId())
                .pedidoId(item.getPedido() != null ? item.getPedido().getPedidoId() : null)
                .productoId(item.getProducto() != null ? item.getProducto().getProductoId() : null)
                .nombreProducto(item.getProducto() != null ? item.getProducto().getNombre() : null)
                .imagenUrl(item.getProducto() != null ? item.getProducto().getImagenUrl() : null)
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(subtotal)
                .build();
    }

    /**
     * Actualiza una entidad PedidoItem existente con los datos de un PedidoItemRequestDTO.
     * 
     * Este método se usa al actualizar un item de pedido existente. Solo actualiza
     * los campos que están presentes en el DTO, preservando:
     * - pedidoItemId: No se modifica
     * - pedido: No se modifica (debe actualizarse por separado si es necesario)
     * - producto: No se modifica (debe actualizarse por separado si es necesario)
     * - precioUnitario: No se modifica (se congela al momento de agregar el item)
     * 
     * IMPORTANTE: Este método NO actualiza:
     * - pedidoItemId: Se preserva el ID original
     * - pedido: La relación con Pedido debe actualizarse por separado en el servicio
     * - producto: La relación con Producto debe actualizarse por separado en el servicio
     * - precioUnitario: Se preserva el precio original (se congela al momento de agregar)
     * 
     * @param item Entidad PedidoItem existente a actualizar
     * @param dto DTO con los nuevos datos
     * 
     * Ejemplo de uso:
     * PedidoItem item = pedidoItemRepository.findById(id).orElseThrow(...);
     * pedidoItemMapper.updateEntityFromDto(item, dto);
     * pedidoItemRepository.save(item);
     */
    public void updateEntityFromDto(PedidoItem item, PedidoItemRequestDTO dto) {
        if (item == null || dto == null) {
            return;
        }

        // Actualizamos solo los campos que pueden modificarse
        if (dto.getCantidad() != null) {
            item.setCantidad(dto.getCantidad());
        }
        // Nota: pedidoId y productoId se manejan por separado en el servicio si es necesario actualizarlos
        // Nota: precioUnitario no se actualiza porque se congela al momento de agregar el item
    }
}

