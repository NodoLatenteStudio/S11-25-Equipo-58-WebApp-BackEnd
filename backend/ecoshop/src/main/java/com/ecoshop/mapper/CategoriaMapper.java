package com.ecoshop.mapper;

import com.ecoshop.domain.Categoria;
import com.ecoshop.dto.Categoria.CategoriaRequestDTO;
import com.ecoshop.dto.Categoria.CategoriaResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Categoria y DTOs.
 * 
 * Esta clase se encarga de transformar objetos entre la capa de dominio (entidades JPA)
 * y la capa de presentación (DTOs).
 */
@Component
public class CategoriaMapper {

    /**
     * Convierte un CategoriaRequestDTO a una entidad Categoria.
     * 
     * @param dto DTO con los datos de la categoría a crear
     * @return Entidad Categoria sin ID asignado
     */
    public Categoria toEntity(CategoriaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Categoria.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }

    /**
     * Convierte una entidad Categoria a un CategoriaResponseDTO.
     * 
     * @param categoria Entidad Categoria a convertir
     * @return DTO de respuesta con los datos de la categoría
     */
    public CategoriaResponseDTO toResponse(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponseDTO.builder()
                .categoriaId(categoria.getCategoriaId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .fechaCreacion(categoria.getFechaCreacion())
                .build();
    }

    /**
     * Actualiza una entidad Categoria existente con los datos de un CategoriaRequestDTO.
     * 
     * @param categoria Entidad Categoria existente a actualizar
     * @param dto DTO con los nuevos datos
     */
    public void updateEntityFromDto(Categoria categoria, CategoriaRequestDTO dto) {
        if (categoria == null || dto == null) {
            return;
        }

        if (dto.getNombre() != null) {
            categoria.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            categoria.setDescripcion(dto.getDescripcion());
        }
    }
}

