package com.ecoshop.mapper;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Usuario y DTOs.
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
 * - El password_hash se maneja en el servicio (hashing), no en el mapper
 * - El clerkId se maneja con Clerk ya integrado
 * - La fecha de registro (fechaRegistro) se genera automáticamente por JPA
 * 
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class UsuarioMapper {

    /**
     * Convierte un UsuarioRequestDTO a una entidad Usuario.
     * 
     * Este método se usa al crear un nuevo usuario. La entidad resultante
     * no tiene ID asignado (se asignará al guardar en la BD) y no tiene
     * el password_hash establecido (debe hashearse en el servicio).
     * 
     * IMPORTANTE: Este método NO establece:
     * - usuarioId: Se asigna automáticamente al guardar en la BD
     * - passwordHash: Debe hashearse en el servicio usando BCrypt o similar
     * - fechaRegistro: Se genera automáticamente por @CreationTimestamp
     * 
     * @param dto DTO con los datos del usuario a crear
     * @return Entidad Usuario sin ID ni password_hash establecido
     * 
     * Ejemplo de uso:
     * UsuarioRequestDTO dto = new UsuarioRequestDTO(...);
     * Usuario usuario = usuarioMapper.toEntity(dto);
     * usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // En el servicio
     * usuarioRepository.save(usuario);
     */
    public Usuario toEntity(UsuarioRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .clerkId(dto.getClerkId())
                .email(dto.getEmail())
                .nombre(dto.getNombre())
                .direccionDefault(dto.getDireccionDefault())
                .rol(dto.getRol())
                // Nota: passwordHash se establece en el servicio después de hashear
                // Nota: fechaRegistro se genera automáticamente por @CreationTimestamp
                .build();
    }

    /**
     * Convierte una entidad Usuario a un UsuarioResponseDTO.
     * 
     * Este método se usa al retornar datos de un usuario al cliente.
     * IMPORTANTE: El password_hash nunca se incluye en el DTO por seguridad.
     * 
     * @param usuario Entidad Usuario a convertir
     * @return DTO de respuesta con los datos del usuario (sin password_hash)
     * 
     * Ejemplo de uso:
     * Usuario usuario = usuarioRepository.findById(id).orElseThrow(...);
     * UsuarioResponseDTO dto = usuarioMapper.toResponse(usuario);
     * return dto;
     */
    public UsuarioResponseDTO toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponseDTO.builder()
                .usuarioId(usuario.getUsuarioId())
                .clerkId(usuario.getClerkId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .direccionDefault(usuario.getDireccionDefault())
                .rol(usuario.getRol())
                .fechaRegistro(usuario.getFechaRegistro())
                // Nota: passwordHash nunca se incluye en el DTO por seguridad
                .build();
    }

    /**
     * Actualiza una entidad Usuario existente con los datos de un UsuarioRequestDTO.
     * 
     * Este método se usa al actualizar un usuario existente. Solo actualiza
     * los campos que están presentes en el DTO, preservando:
     * - usuarioId: No se modifica
     * - passwordHash: No se modifica (debe actualizarse por separado por seguridad)
     * - fechaRegistro: No se modifica (es inmutable)
     * 
     * IMPORTANTE: Este método NO actualiza:
     * - usuarioId: Se preserva el ID original
     * - passwordHash: La contraseña debe actualizarse por separado por seguridad
     * - fechaRegistro: Se preserva la fecha original (es inmutable)
     * 
     * @param usuario Entidad Usuario existente a actualizar
     * @param dto DTO con los nuevos datos
     * 
     * Ejemplo de uso:
     * Usuario usuario = usuarioRepository.findById(id).orElseThrow(...);
     * usuarioMapper.updateEntityFromDto(usuario, dto);
     * usuarioRepository.save(usuario);
     */
    public void updateEntityFromDto(Usuario usuario, UsuarioRequestDTO dto) {
        if (usuario == null || dto == null) {
            return;
        }

        // Actualizamos solo los campos que pueden modificarse
        if (dto.getClerkId() != null) {
            usuario.setClerkId(dto.getClerkId());
        }
        if (dto.getEmail() != null) {
            usuario.setEmail(dto.getEmail());
        }
        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        if (dto.getDireccionDefault() != null) {
            usuario.setDireccionDefault(dto.getDireccionDefault());
        }
        if (dto.getRol() != null) {
            usuario.setRol(dto.getRol());
        }
        // Nota: password se maneja por separado en el servicio por seguridad
        // Nota: fechaRegistro no se actualiza (es inmutable)
    }
}

