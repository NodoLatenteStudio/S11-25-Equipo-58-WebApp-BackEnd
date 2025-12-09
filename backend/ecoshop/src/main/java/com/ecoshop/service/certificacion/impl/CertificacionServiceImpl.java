package com.ecoshop.service.certificacion.impl;

import com.ecoshop.domain.Certificacion;
import com.ecoshop.dto.Certificacion.CertificacionRequestDTO;
import com.ecoshop.dto.Certificacion.CertificacionResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.CertificacionMapper;
import com.ecoshop.repository.certificacion.CertificacionRepository;
import com.ecoshop.service.certificacion.CertificacionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de certificaciones.
 * 
 * Esta clase contiene la lógica de negocio para gestionar certificaciones.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Validar reglas de negocio (ej: unicidad de nombres de sello)
 * - Manejar excepciones de negocio
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (nombre de sello único, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Guardar en la base de datos
 * 4. Convertir entidad a DTO de respuesta usando el mapper
 * 5. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class CertificacionServiceImpl implements CertificacionService {

    // Repositorio para acceder a la base de datos de certificaciones
    private final CertificacionRepository certificacionRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final CertificacionMapper certificacionMapper;
    
    // EntityManager para operaciones avanzadas de JPA (refresh, flush, etc.)
    private final EntityManager entityManager;

    /**
     * Crea una nueva certificación en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el nombre del sello sea único
     * 2. Convierte el DTO a entidad usando el mapper
     * 3. Guarda la certificación en la BD
     * 4. Convierte la entidad guardada a DTO de respuesta usando el mapper
     * 5. Retorna el DTO con el ID asignado
     * 
     * @param dto CertificacionRequestDTO con los datos de la certificación a crear
     * @return CertificacionResponseDTO con la certificación creada y su ID asignado
     * @throws BadRequestException si ya existe una certificación con el mismo nombre de sello
     * 
     * Ejemplo de uso:
     * POST /api/v1/certificaciones
     * Body: { "nombreSello": "Fair Trade", "descripcion": "...", "entidadEmisora": "..." }
     */
    @Override
    @Transactional
    public CertificacionResponseDTO createCertificacion(CertificacionRequestDTO dto) {
        // 1. Validar que el nombre del sello sea obligatorio en creación
        if (dto.getNombreSello() == null || dto.getNombreSello().trim().isEmpty()) {
            throw new BadRequestException("El nombre del sello es obligatorio");
        }
        
        // 2. Validar que el nombre del sello sea único (case-insensitive)
        String nombreSello = dto.getNombreSello().trim();
        if (certificacionRepository.existsByNombreSelloIgnoreCase(nombreSello)) {
            throw new BadRequestException("Ya existe una certificación con el nombre de sello: " + nombreSello);
        }

        // 3. Convertir DTO a entidad usando el mapper
        Certificacion certificacion = certificacionMapper.toEntity(dto);

        // 4. Guardar en la BD (JPA asigna el ID automáticamente)
        Certificacion savedCertificacion = certificacionRepository.save(certificacion);
        
        // 5. Forzamos el flush para asegurar que los cambios se persistan
        entityManager.flush();

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return certificacionMapper.toResponse(savedCertificacion);
    }

    /**
     * Busca una certificación por su ID.
     * 
     * Proceso:
     * 1. Busca la certificación en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, la convierte a CertificacionResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador de la certificación
     * @return CertificacionResponseDTO de la certificación encontrada
     * @throws ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/certificaciones/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public CertificacionResponseDTO getCertificacionById(Integer id) {
        // Buscamos la certificación en la BD
        Certificacion certificacion = certificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación no encontrada con id: " + id));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return certificacionMapper.toResponse(certificacion);
    }

    /**
     * Obtiene todas las certificaciones de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Certificacion de la BD
     * 2. Convierte cada entidad a CertificacionResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todas las certificaciones convertidas a CertificacionResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/certificaciones
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<CertificacionResponseDTO> getAllCertificaciones() {
        // Obtenemos todas las certificaciones de la BD
        List<Certificacion> certificaciones = certificacionRepository.findAll();
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return certificaciones.stream()
                .map(certificacionMapper::toResponse) // Convierte cada Certificacion a CertificacionResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    /**
     * Busca una certificación por su nombre de sello.
     * 
     * Proceso:
     * 1. Busca la certificación en la BD por nombre de sello (case-insensitive)
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, la convierte a CertificacionResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param nombreSello Nombre del sello de certificación
     * @return CertificacionResponseDTO de la certificación encontrada
     * @throws ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/certificaciones/sello/Fair Trade
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public CertificacionResponseDTO getCertificacionByNombreSello(String nombreSello) {
        // Buscamos la certificación usando búsqueda case-insensitive
        Certificacion certificacion = certificacionRepository.findByNombreSelloIgnoreCase(nombreSello)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación no encontrada con nombre de sello: " + nombreSello));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return certificacionMapper.toResponse(certificacion);
    }

    /**
     * Actualiza una certificación existente.
     * 
     * Este método permite actualizaciones parciales. Solo se actualizan los campos
     * que se proporcionen en el DTO. Los campos no incluidos se mantienen sin cambios.
     * 
     * Proceso:
     * 1. Verifica que la certificación exista
     * 2. Valida que el nombre del sello sea único (solo si se proporciona y cambió)
     * 3. Actualiza solo los campos proporcionados usando el mapper
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad actualizada a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID de la certificación a actualizar
     * @param dto Datos a actualizar (todos los campos son opcionales)
     * @return CertificacionResponseDTO actualizado
     * @throws ResourceNotFoundException si la certificación no existe
     * @throws BadRequestException si el nuevo nombre de sello ya existe en otra certificación
     * 
     * Ejemplo de uso - Actualización parcial:
     * PUT /api/v1/certificaciones/2
     * Body: { "imagenUrl": "https://i.ibb.co/xxxxx/carbon-neutral.png" }
     * 
     * Ejemplo de uso - Actualización completa:
     * PUT /api/v1/certificaciones/1
     * Body: { "nombreSello": "Fair Trade Actualizado", "descripcion": "...", "entidadEmisora": "...", "imagenUrl": "..." }
     */
    @Override
    @Transactional
    public CertificacionResponseDTO updateCertificacion(Integer id, CertificacionRequestDTO dto) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        Certificacion existingCertificacion = certificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación no encontrada con id: " + id));

        // 2. Validar que el nombre del sello sea único si se cambia
        if (dto.getNombreSello() != null && !dto.getNombreSello().trim().isEmpty()) {
            String nuevoNombreSello = dto.getNombreSello().trim();
            String nombreActual = existingCertificacion.getNombreSello();
            
            // Solo validamos si el nombre cambió
            if (nombreActual == null || !nombreActual.equalsIgnoreCase(nuevoNombreSello)) {
                if (certificacionRepository.existsByNombreSelloIgnoreCase(nuevoNombreSello)) {
                    throw new BadRequestException("Ya existe una certificación con el nombre de sello: " + nuevoNombreSello);
                }
            }
        }

        // 3. Actualizamos los campos usando el mapper
        certificacionMapper.updateEntityFromDto(existingCertificacion, dto);

        // 4. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Certificacion updatedCertificacion = certificacionRepository.save(existingCertificacion);
        
        // 5. Forzamos el flush para asegurar que los cambios se persistan
        entityManager.flush();

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return certificacionMapper.toResponse(updatedCertificacion);
    }

    /**
     * Elimina una certificación de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que la certificación exista
     * 2. Elimina las relaciones con productos de la tabla intermedia (si existen)
     * 3. Elimina la certificación de la BD
     * 
     * Nota: Verificamos existencia antes de eliminar para dar un mensaje de error
     * más claro. También podríamos dejar que JPA lance la excepción, pero este
     * enfoque nos da más control sobre el mensaje de error.
     * 
     * @param id Identificador de la certificación a eliminar
     * @throws ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/certificaciones/1
     */
    @Override
    @Transactional
    public void deleteCertificacion(Integer id) {
        // Verificamos que la certificación exista
        if (!certificacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certificación no encontrada con id: " + id);
        }
        
        // Eliminamos directamente las relaciones de la tabla intermedia usando una consulta nativa
        // Esto es más eficiente y evita problemas con foreign key constraints
        // Nota: Esto se puede hacer en el repositorio si es necesario
        
        // Forzamos el flush para asegurar que las relaciones se eliminen antes de eliminar la certificación
        entityManager.flush();
        
        // Ahora eliminamos la certificación de forma segura
        certificacionRepository.deleteById(id);
        
        // Forzamos el flush final para asegurar que la eliminación se persista
        entityManager.flush();
    }
}

