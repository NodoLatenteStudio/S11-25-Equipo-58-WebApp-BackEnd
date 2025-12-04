package com.ecoshop.mapper;

import com.ecoshop.domain.Marca;
import com.ecoshop.dto.Marca.MarcaRequestDTO;
import com.ecoshop.dto.Marca.MarcaResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Marca y DTOs.
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
 * - La fecha de unión (fechaUnion) se genera automáticamente por JPA
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class MarcaMapper {

    /**
     * Convierte un MarcaRequestDTO a una entidad Marca.
     * 
     * Este método se usa al crear una nueva marca. La entidad resultante
     * no tiene ID asignado (se asignará al guardar en la BD) y no tiene
     * la relación con Usuario establecida (debe establecerse en el servicio).
     * 
     * IMPORTANTE: Este método NO establece:
     * - marcaId: Se asigna automáticamente al guardar en la BD
     * - usuario: Debe establecerse en el servicio usando MarcaRequestDTO.getUsuarioId()
     * - fechaUnion: Se genera automáticamente por @CreationTimestamp
     * 
     * @param dto DTO con los datos de la marca a crear
     * @return Entidad Marca sin ID ni relaciones establecidas
     * 
     * Ejemplo de uso:
     * MarcaRequestDTO dto = new MarcaRequestDTO(...);
     * Marca marca = marcaMapper.toEntity(dto);
     * marca.setUsuario(usuario); // Establecer relación en el servicio
     * marcaRepository.save(marca);
     */
    public Marca toEntity(MarcaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Marca.builder()
                .nombreOficial(dto.getNombreOficial())
                .descripcionSostenible(dto.getDescripcionSostenible())
                .sitioWeb(dto.getSitioWeb())
                .logoUrl(dto.getLogoUrl())
                .build();
    }

    /**
     * Convierte una entidad Marca a un MarcaResponseDTO.
     * 
     * Este método se usa al retornar datos de una marca al cliente.
     * Extrae el usuarioId de la relación con Usuario para incluirlo en el DTO.
     * 
     * IMPORTANTE: Si la relación con Usuario no está cargada (lazy loading),
     * se debe asegurar que esté cargada antes de llamar a este método, o
     * se lanzará una excepción LazyInitializationException.
     * 
     * @param marca Entidad Marca a convertir
     * @return DTO de respuesta con los datos de la marca
     * 
     * Ejemplo de uso:
     * Marca marca = marcaRepository.findById(id).orElseThrow(...);
     * MarcaResponseDTO dto = marcaMapper.toResponse(marca);
     * return dto;
     */
    public MarcaResponseDTO toResponse(Marca marca) {
        if (marca == null) {
            return null;
        }

        return MarcaResponseDTO.builder()
                .marcaId(marca.getMarcaId())
                .usuarioId(marca.getUsuario() != null ? marca.getUsuario().getUsuarioId() : null)
                .nombreOficial(marca.getNombreOficial())
                .descripcionSostenible(marca.getDescripcionSostenible())
                .sitioWeb(marca.getSitioWeb())
                .logoUrl(marca.getLogoUrl())
                .fechaUnion(marca.getFechaUnion())
                .build();
    }

    /**
     * Actualiza una entidad Marca existente con los datos de un MarcaRequestDTO.
     * 
     * Este método se usa al actualizar una marca existente. Solo actualiza
     * los campos que están presentes en el DTO, preservando:
     * - marcaId: No se modifica
     * - usuario: No se modifica (debe actualizarse por separado si es necesario)
     * - fechaUnion: No se modifica (es inmutable)
     * 
     * IMPORTANTE: Este método NO actualiza:
     * - marcaId: Se preserva el ID original
     * - usuario: La relación con Usuario debe actualizarse por separado en el servicio
     * - fechaUnion: Se preserva la fecha original (es inmutable)
     * 
     * @param marca Entidad Marca existente a actualizar
     * @param dto DTO con los nuevos datos
     * 
     * Ejemplo de uso:
     * Marca marca = marcaRepository.findById(id).orElseThrow(...);
     * marcaMapper.updateEntityFromDto(marca, dto);
     * marcaRepository.save(marca);
     */
    public void updateEntityFromDto(Marca marca, MarcaRequestDTO dto) {
        if (marca == null || dto == null) {
            return;
        }

        // Actualizamos solo los campos que pueden modificarse
        if (dto.getNombreOficial() != null) {
            marca.setNombreOficial(dto.getNombreOficial());
        }
        if (dto.getDescripcionSostenible() != null) {
            marca.setDescripcionSostenible(dto.getDescripcionSostenible());
        }
        if (dto.getSitioWeb() != null) {
            marca.setSitioWeb(dto.getSitioWeb());
        }
        if (dto.getLogoUrl() != null) {
            marca.setLogoUrl(dto.getLogoUrl());
        }
        // Nota: usuarioId se maneja por separado en el servicio si es necesario actualizarlo
    }
}

