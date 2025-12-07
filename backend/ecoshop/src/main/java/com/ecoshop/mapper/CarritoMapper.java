package com.ecoshop.mapper;

import com.ecoshop.domain.Carrito;
import com.ecoshop.domain.CarritoItem;
import com.ecoshop.dto.Carrito.CarritoItemResponseDTO;
import com.ecoshop.dto.Carrito.CarritoResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Carrito/CarritoItem y DTOs.
 * 
 * Esta clase se encarga de transformar objetos entre la capa de dominio (entidades JPA)
 * y la capa de presentación (DTOs).
 */
@Component
public class CarritoMapper {

    /**
     * Convierte una entidad Carrito a un CarritoResponseDTO.
     * 
     * @param carrito Entidad Carrito a convertir
     * @param items Lista de items del carrito convertidos a DTOs
     * @return DTO de respuesta con los datos del carrito
     */
    public CarritoResponseDTO toResponse(Carrito carrito, List<CarritoItemResponseDTO> items) {
        if (carrito == null) {
            return null;
        }

        return CarritoResponseDTO.builder()
                .carritoId(carrito.getCarritoId())
                .usuarioId(carrito.getUsuario() != null ? carrito.getUsuario().getUsuarioId() : null)
                .total(carrito.getTotal())
                .fechaCreacion(carrito.getFechaCreacion())
                .fechaActualizacion(carrito.getFechaActualizacion())
                .items(items != null ? items : List.of())
                .build();
    }

    /**
     * Convierte una entidad CarritoItem a un CarritoItemResponseDTO.
     * 
     * @param item Entidad CarritoItem a convertir
     * @return DTO de respuesta con los datos del item
     */
    public CarritoItemResponseDTO toItemResponse(CarritoItem item) {
        if (item == null) {
            return null;
        }

        // Calculamos el subtotal (cantidad * precioUnitario)
        BigDecimal subtotal = item.getPrecioUnitario()
                .multiply(new BigDecimal(item.getCantidad()));

        return CarritoItemResponseDTO.builder()
                .carritoItemId(item.getCarritoItemId())
                .carritoId(item.getCarrito() != null ? item.getCarrito().getCarritoId() : null)
                .productoId(item.getProducto() != null ? item.getProducto().getProductoId() : null)
                .nombreProducto(item.getProducto() != null ? item.getProducto().getNombre() : null)
                .imagenUrl(item.getProducto() != null ? item.getProducto().getImagenUrl() : null)
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(subtotal)
                .build();
    }

    /**
     * Convierte una lista de entidades CarritoItem a una lista de DTOs.
     * 
     * @param items Lista de entidades CarritoItem
     * @return Lista de DTOs de respuesta
     */
    public List<CarritoItemResponseDTO> toItemResponseList(List<CarritoItem> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }
}

