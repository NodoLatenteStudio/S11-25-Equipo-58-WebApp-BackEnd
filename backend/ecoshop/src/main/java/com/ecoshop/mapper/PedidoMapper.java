package com.ecoshop.mapper;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.Pedido.PedidoRequestDTO;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Pedido y DTOs.
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
 * - La relación con Usuario se maneja en el servicio, no en el mapper
 * - La fecha de pedido (fechaPedido) se genera automáticamente por JPA
 * - El total del pedido generalmente se calcula a partir de los PedidoItems
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class PedidoMapper {

    /**
     * Convierte un PedidoRequestDTO a una entidad Pedido.
     * 
     * Este método se usa al crear un nuevo pedido. La entidad resultante
     * no tiene ID asignado (se asignará al guardar en la BD) y no tiene
     * la relación con Usuario establecida (debe establecerse en el servicio).
     * 
     * IMPORTANTE: Este método NO establece:
     * - pedidoId: Se asigna automáticamente al guardar en la BD
     * - usuario: Debe establecerse en el servicio usando PedidoRequestDTO.getUsuarioId()
     * - fechaPedido: Se genera automáticamente por @CreationTimestamp
     * - total: Generalmente se calcula a partir de los PedidoItems, por defecto se usa BigDecimal.ZERO
     * 
     * @param dto DTO con los datos del pedido a crear
     * @return Entidad Pedido sin ID ni relaciones establecidas
     * 
     * Ejemplo de uso:
     * PedidoRequestDTO dto = new PedidoRequestDTO(...);
     * Pedido pedido = pedidoMapper.toEntity(dto);
     * pedido.setUsuario(usuario); // Establecer relación en el servicio
     * pedido.setTotal(BigDecimal.ZERO); // Inicializar total
     * pedidoRepository.save(pedido);
     */
    public Pedido toEntity(PedidoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Pedido.builder()
                .estado(dto.getEstado() != null ? dto.getEstado() : "pendiente_pago")
                .direccionEnvio(dto.getDireccionEnvio())
                .metodoPago(dto.getMetodoPago())
                .idTransaccionPago(dto.getIdTransaccionPago())
                .huellaCarbonoTotalKg(dto.getHuellaCarbonoTotalKg() != null 
                        ? dto.getHuellaCarbonoTotalKg() 
                        : java.math.BigDecimal.ZERO)
                .total(java.math.BigDecimal.ZERO) // Se calcula a partir de los items
                .build();
    }

    /**
     * Convierte una entidad Pedido a un PedidoResponseDTO.
     * 
     * Este método se usa al retornar datos de un pedido al cliente.
     * Extrae el usuarioId y emailUsuario de la relación con Usuario para incluirlos en el DTO.
     * 
     * IMPORTANTE: Si la relación con Usuario no está cargada (lazy loading),
     * se debe asegurar que esté cargada antes de llamar a este método, o
     * se lanzará una excepción LazyInitializationException.
     * 
     * @param pedido Entidad Pedido a convertir
     * @return DTO de respuesta con los datos del pedido
     * 
     * Ejemplo de uso:
     * Pedido pedido = pedidoRepository.findById(id).orElseThrow(...);
     * PedidoResponseDTO dto = pedidoMapper.toResponse(pedido);
     * return dto;
     */
    public PedidoResponseDTO toResponse(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        return PedidoResponseDTO.builder()
                .pedidoId(pedido.getPedidoId())
                .usuarioId(pedido.getUsuario() != null ? pedido.getUsuario().getUsuarioId() : null)
                .emailUsuario(pedido.getUsuario() != null ? pedido.getUsuario().getEmail() : null)
                .fechaPedido(pedido.getFechaPedido())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .direccionEnvio(pedido.getDireccionEnvio())
                .metodoPago(pedido.getMetodoPago())
                .idTransaccionPago(pedido.getIdTransaccionPago())
                .huellaCarbonoTotalKg(pedido.getHuellaCarbonoTotalKg())
                .build();
    }

    /**
     * Actualiza una entidad Pedido existente con los datos de un PedidoRequestDTO.
     * 
     * Este método se usa al actualizar un pedido existente. Solo actualiza
     * los campos que están presentes en el DTO, preservando:
     * - pedidoId: No se modifica
     * - usuario: No se modifica (debe actualizarse por separado si es necesario)
     * - fechaPedido: No se modifica (es inmutable)
     * - total: No se modifica (se calcula a partir de los items)
     * 
     * IMPORTANTE: Este método NO actualiza:
     * - pedidoId: Se preserva el ID original
     * - usuario: La relación con Usuario debe actualizarse por separado en el servicio
     * - fechaPedido: Se preserva la fecha original (es inmutable)
     * - total: Se preserva el total original (se calcula a partir de los items)
     * 
     * @param pedido Entidad Pedido existente a actualizar
     * @param dto DTO con los nuevos datos
     * 
     * Ejemplo de uso:
     * Pedido pedido = pedidoRepository.findById(id).orElseThrow(...);
     * pedidoMapper.updateEntityFromDto(pedido, dto);
     * pedidoRepository.save(pedido);
     */
    public void updateEntityFromDto(Pedido pedido, PedidoRequestDTO dto) {
        if (pedido == null || dto == null) {
            return;
        }

        // Actualizamos solo los campos que pueden modificarse
        if (dto.getEstado() != null) {
            pedido.setEstado(dto.getEstado());
        }
        if (dto.getDireccionEnvio() != null) {
            pedido.setDireccionEnvio(dto.getDireccionEnvio());
        }
        if (dto.getMetodoPago() != null) {
            pedido.setMetodoPago(dto.getMetodoPago());
        }
        if (dto.getIdTransaccionPago() != null) {
            pedido.setIdTransaccionPago(dto.getIdTransaccionPago());
        }
        if (dto.getHuellaCarbonoTotalKg() != null) {
            pedido.setHuellaCarbonoTotalKg(dto.getHuellaCarbonoTotalKg());
        }
        // Nota: usuarioId se maneja por separado en el servicio si es necesario actualizarlo
        // Nota: total se calcula a partir de los items, no se actualiza desde el DTO
    }
}

