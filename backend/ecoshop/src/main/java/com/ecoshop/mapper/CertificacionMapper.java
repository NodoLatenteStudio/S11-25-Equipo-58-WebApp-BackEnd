package com.ecoshop.mapper;

import com.ecoshop.domain.Certificacion;
import com.ecoshop.dto.CertificacionRequestDTO;
import com.ecoshop.dto.CertificacionResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Certificacion y DTOs.
 * 
 * Esta clase se encarga de convertir entre:
 * - Certificacion (entidad JPA) ↔ CertificacionRequestDTO (datos de entrada)
 * - Certificacion (entidad JPA) ↔ CertificacionResponseDTO (datos de salida)
 * 
 * ¿Por qué usar un Mapper?
 * - Separación de responsabilidades: el mapper se encarga solo de la conversión
 * - Reutilización: el mismo mapper se usa en diferentes partes del código
 * - Mantenibilidad: cambios en la estructura se hacen en un solo lugar
 * - Testabilidad: fácil de probar de forma aislada
 * 
 * Conversiones realizadas:
 * 1. toEntity: Convierte CertificacionRequestDTO a Certificacion
 * 2. toResponse: Convierte Certificacion a CertificacionResponseDTO
 * 3. updateEntityFromDto: Actualiza una entidad Certificacion existente desde un DTO
 * 
 * Notas importantes:
 * - El ID (certificacionId) se asigna automáticamente por la BD al crear
 * - Los campos se mapean directamente según el esquema de la base de datos
 * - Las relaciones (productos) no se incluyen en los DTOs para evitar referencias circulares
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class CertificacionMapper {

    /**
     * Convierte un CertificacionRequestDTO a una entidad Certificacion.
     * 
     * Este método crea una nueva entidad Certificacion a partir de los datos del DTO.
     * 
     * Proceso:
     * 1. Crea una nueva instancia de Certificacion
     * 2. Copia los campos básicos del DTO a la entidad
     * 3. Retorna la entidad (sin ID, sin productos)
     * 
     * @param dto CertificacionRequestDTO con los datos de la certificación
     * @return Certificacion entidad sin ID ni productos asignados
     * 
     * Nota: El ID se asignará automáticamente por la BD cuando se guarde.
     * Los productos se asignan en el servicio cuando se asocian a productos.
     */
    public Certificacion toEntity(CertificacionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Certificacion.builder()
                .nombreSello(dto.getNombreSello())
                .descripcion(dto.getDescripcion())
                .entidadEmisora(dto.getEntidadEmisora())
                .build();
    }

    /**
     * Convierte una entidad Certificacion a un CertificacionResponseDTO.
     * 
     * Este método crea un DTO de respuesta a partir de la entidad Certificacion.
     * Incluye todos los campos, pero no incluye las relaciones (productos).
     * 
     * Proceso:
     * 1. Crea un nuevo CertificacionResponseDTO
     * 2. Copia los campos básicos de la entidad al DTO
     * 3. Retorna el DTO completo
     * 
     * @param certificacion Entidad Certificacion con todos sus datos cargados
     * @return CertificacionResponseDTO con todos los datos de la certificación
     * 
     * Nota: La entidad Certificacion puede tener productos cargados, pero estos
     * no se incluyen en el DTO para evitar referencias circulares.
     */
    public CertificacionResponseDTO toResponse(Certificacion certificacion) {
        if (certificacion == null) {
            return null;
        }

        return CertificacionResponseDTO.builder()
                .certificacionId(certificacion.getCertificacionId())
                .nombreSello(certificacion.getNombreSello())
                .descripcion(certificacion.getDescripcion())
                .entidadEmisora(certificacion.getEntidadEmisora())
                .build();
    }

    /**
     * Actualiza una entidad Certificacion existente con los datos de un CertificacionRequestDTO.
     * 
     * Este método actualiza los campos de una entidad Certificacion existente
     * con los valores de un DTO, preservando campos importantes como el ID
     * y las relaciones.
     * 
     * Proceso:
     * 1. Actualiza los campos básicos de la certificación desde el DTO
     * 2. NO actualiza el ID (se mantiene el original)
     * 3. NO actualiza las relaciones (productos) - se manejan en el servicio
     * 
     * @param certificacion Entidad Certificacion existente a actualizar
     * @param dto CertificacionRequestDTO con los nuevos datos
     * 
     * Nota: Este método solo actualiza los campos básicos. Las relaciones
     * (productos) deben actualizarse en el servicio si es necesario.
     */
    public void updateEntityFromDto(Certificacion certificacion, CertificacionRequestDTO dto) {
        if (certificacion == null || dto == null) {
            return;
        }

        // Actualizar campos básicos (preservando ID y relaciones)
        certificacion.setNombreSello(dto.getNombreSello());
        certificacion.setDescripcion(dto.getDescripcion());
        certificacion.setEntidadEmisora(dto.getEntidadEmisora());
    }
}

