package com.ecoshop.mapper;

import com.ecoshop.domain.ContenidoEducativo;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoRequestDTO;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades y DTOs de ContenidoEducativo.
 */
@Component
public class ContenidoEducativoMapper {
    
    /**
     * Convierte un DTO de solicitud a una entidad.
     */
    public ContenidoEducativo toEntity(ContenidoEducativoRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return ContenidoEducativo.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .contenido(dto.getContenido())
                .categoria(dto.getCategoria())
                .imagenUrl(dto.getImagenUrl())
                .autor(dto.getAutor())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
    
    /**
     * Convierte una entidad a un DTO de respuesta.
     */
    public ContenidoEducativoResponseDTO toResponseDTO(ContenidoEducativo contenido) {
        if (contenido == null) {
            return null;
        }
        
        return ContenidoEducativoResponseDTO.builder()
                .contenidoId(contenido.getContenidoId())
                .titulo(contenido.getTitulo())
                .descripcion(contenido.getDescripcion())
                .contenido(contenido.getContenido())
                .categoria(contenido.getCategoria())
                .imagenUrl(contenido.getImagenUrl())
                .autor(contenido.getAutor())
                .fechaCreacion(contenido.getFechaCreacion())
                .fechaActualizacion(contenido.getFechaActualizacion())
                .activo(contenido.getActivo())
                .build();
    }
    
    /**
     * Actualiza una entidad existente con los datos de un DTO.
     */
    public void updateEntityFromDTO(ContenidoEducativo contenido, ContenidoEducativoRequestDTO dto) {
        if (dto == null || contenido == null) {
            return;
        }
        
        if (dto.getTitulo() != null) {
            contenido.setTitulo(dto.getTitulo());
        }
        if (dto.getDescripcion() != null) {
            contenido.setDescripcion(dto.getDescripcion());
        }
        if (dto.getContenido() != null) {
            contenido.setContenido(dto.getContenido());
        }
        if (dto.getCategoria() != null) {
            contenido.setCategoria(dto.getCategoria());
        }
        if (dto.getImagenUrl() != null) {
            contenido.setImagenUrl(dto.getImagenUrl());
        }
        if (dto.getAutor() != null) {
            contenido.setAutor(dto.getAutor());
        }
        if (dto.getActivo() != null) {
            contenido.setActivo(dto.getActivo());
        }
    }
}

