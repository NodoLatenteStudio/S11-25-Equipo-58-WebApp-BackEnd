package com.ecoshop.mapper;

import com.ecoshop.domain.Canje;
import com.ecoshop.dto.Canje.CanjeResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Canje y DTOs.
 * 
 * Esta clase se encarga de transformar objetos entre la capa de dominio (entidades JPA)
 * y la capa de presentación (DTOs).
 */
@Component
public class CanjeMapper {

    /**
     * Convierte una entidad Canje a un CanjeResponseDTO.
     * 
     * @param canje Entidad Canje a convertir
     * @return DTO de respuesta con los datos del canje
     */
    public CanjeResponseDTO toResponse(Canje canje) {
        if (canje == null) {
            return null;
        }

        return CanjeResponseDTO.builder()
                .canjeId(canje.getCanjeId())
                .usuarioId(canje.getUsuario() != null ? canje.getUsuario().getUsuarioId() : null)
                .recompensaId(canje.getRecompensa() != null ? canje.getRecompensa().getRecompensaId() : null)
                .nombreRecompensa(canje.getRecompensa() != null ? canje.getRecompensa().getNombre() : null)
                .puntosUsados(canje.getPuntosUsados())
                .estado(canje.getEstado())
                .codigoCanje(canje.getCodigoCanje())
                .fechaCanje(canje.getFechaCanje())
                .build();
    }
}

