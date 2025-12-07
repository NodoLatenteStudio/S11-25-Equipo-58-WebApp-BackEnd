package com.ecoshop.mapper;

import com.ecoshop.domain.Recompensa;
import com.ecoshop.dto.Recompensa.RecompensaRequestDTO;
import com.ecoshop.dto.Recompensa.RecompensaResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Recompensa y DTOs.
 * 
 * Esta clase se encarga de transformar objetos entre la capa de dominio (entidades JPA)
 * y la capa de presentación (DTOs).
 */
@Component
public class RecompensaMapper {

    /**
     * Convierte un RecompensaRequestDTO a una entidad Recompensa.
     * 
     * @param dto DTO con los datos de la recompensa a crear
     * @return Entidad Recompensa sin ID asignado
     */
    public Recompensa toEntity(RecompensaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Recompensa.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .puntosRequeridos(dto.getPuntosRequeridos())
                .tipo(dto.getTipo())
                .valor(dto.getValor())
                .stockDisponible(dto.getStockDisponible())
                .imagenUrl(dto.getImagenUrl())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }

    /**
     * Convierte una entidad Recompensa a un RecompensaResponseDTO.
     * 
     * @param recompensa Entidad Recompensa a convertir
     * @return DTO de respuesta con los datos de la recompensa
     */
    public RecompensaResponseDTO toResponse(Recompensa recompensa) {
        if (recompensa == null) {
            return null;
        }

        return RecompensaResponseDTO.builder()
                .recompensaId(recompensa.getRecompensaId())
                .nombre(recompensa.getNombre())
                .descripcion(recompensa.getDescripcion())
                .puntosRequeridos(recompensa.getPuntosRequeridos())
                .tipo(recompensa.getTipo())
                .valor(recompensa.getValor())
                .stockDisponible(recompensa.getStockDisponible())
                .imagenUrl(recompensa.getImagenUrl())
                .activo(recompensa.getActivo())
                .fechaCreacion(recompensa.getFechaCreacion())
                .fechaActualizacion(recompensa.getFechaActualizacion())
                .build();
    }

    /**
     * Actualiza una entidad Recompensa existente con los datos de un RecompensaRequestDTO.
     * 
     * @param recompensa Entidad Recompensa existente a actualizar
     * @param dto DTO con los nuevos datos
     */
    public void updateEntityFromDto(Recompensa recompensa, RecompensaRequestDTO dto) {
        if (recompensa == null || dto == null) {
            return;
        }

        if (dto.getNombre() != null) {
            recompensa.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            recompensa.setDescripcion(dto.getDescripcion());
        }
        if (dto.getPuntosRequeridos() != null) {
            recompensa.setPuntosRequeridos(dto.getPuntosRequeridos());
        }
        if (dto.getTipo() != null) {
            recompensa.setTipo(dto.getTipo());
        }
        if (dto.getValor() != null) {
            recompensa.setValor(dto.getValor());
        }
        if (dto.getStockDisponible() != null) {
            recompensa.setStockDisponible(dto.getStockDisponible());
        }
        if (dto.getImagenUrl() != null) {
            recompensa.setImagenUrl(dto.getImagenUrl());
        }
        if (dto.getActivo() != null) {
            recompensa.setActivo(dto.getActivo());
        }
    }
}

