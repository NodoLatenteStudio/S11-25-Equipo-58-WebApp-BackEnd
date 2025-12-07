package com.ecoshop.service.marca.impl;

import com.ecoshop.domain.Marca;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Marca.MarcaRequestDTO;
import com.ecoshop.dto.Marca.MarcaResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.MarcaMapper;
import com.ecoshop.repository.marca.MarcaRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.marca.MarcaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de marcas.
 * 
 * Esta clase contiene la lógica de negocio para gestionar marcas.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Manejar excepciones de negocio
 * - Gestionar relaciones (usuario)
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (usuario existe, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Asignar relaciones (usuario)
 * 4. Guardar en la base de datos
 * 5. Convertir entidad a DTO de respuesta usando el mapper
 * 6. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class MarcaServiceImpl implements MarcaService {

    // Repositorio para acceder a la base de datos de marcas
    private final MarcaRepository marcaRepository;
    
    // Repositorio para acceder a los usuarios
    private final UsuarioRepository usuarioRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final MarcaMapper marcaMapper;

    /**
     * Crea una nueva marca en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Valida que el usuario no tenga ya una marca (relación One-to-One)
     * 3. Convierte el DTO a entidad usando el mapper
     * 4. Asigna la relación con Usuario
     * 5. Guarda la marca en la BD
     * 6. Convierte la entidad a DTO de respuesta usando el mapper
     * 7. Retorna el DTO con el ID asignado
     * 
     * @param dto MarcaRequestDTO con los datos de la marca a crear
     * @return MarcaResponseDTO con la marca creada y su ID asignado
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws BadRequestException si el usuario ya tiene una marca asociada
     * 
     * Ejemplo de uso:
     * POST /api/v1/marcas
     * Body: { "usuarioId": 1, "nombreOficial": "EcoLife", ... }
     */
    @Override
    @Transactional
    public MarcaResponseDTO createMarca(MarcaRequestDTO dto) {
        // 1. Validar existencia del usuario
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        // 2. Validar que el usuario no tenga ya una marca (relación One-to-One)
        List<Marca> marcasExistentes = marcaRepository.findByUsuario_UsuarioId(dto.getUsuarioId());
        if (!marcasExistentes.isEmpty()) {
            Marca marcaExistente = marcasExistentes.get(0);
            throw new BadRequestException(
                String.format("El usuario con ID %d ya tiene una marca asociada (marca ID: %d, nombre: '%s'). " +
                            "Cada usuario solo puede tener una marca. Si deseas modificar la marca existente, usa PUT /api/v1/marcas/%d",
                            dto.getUsuarioId(), marcaExistente.getMarcaId(), marcaExistente.getNombreOficial(), marcaExistente.getMarcaId())
            );
        }

        // 3. Convertir DTO a entidad usando el mapper
        Marca marca = marcaMapper.toEntity(dto);
        
        // 4. Asignar la relación con Usuario
        marca.setUsuario(usuario);

        // 5. Guardar en la BD (JPA asigna el ID automáticamente)
        Marca savedMarca = marcaRepository.save(marca);

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return marcaMapper.toResponse(savedMarca);
    }

    /**
     * Busca una marca por su ID.
     * 
     * Proceso:
     * 1. Busca la marca en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, la convierte a MarcaResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador de la marca
     * @return MarcaResponseDTO de la marca encontrada
     * @throws ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/marcas/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public MarcaResponseDTO getMarcaById(Integer id) {
        // Buscamos la marca en la BD
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return marcaMapper.toResponse(marca);
    }

    /**
     * Obtiene todas las marcas de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Marca de la BD
     * 2. Convierte cada entidad a MarcaResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todas las marcas convertidas a MarcaResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/marcas
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<MarcaResponseDTO> getAllMarcas() {
        // Obtenemos todas las marcas de la BD
        List<Marca> marcas = marcaRepository.findAll();
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return marcas.stream()
                .map(marcaMapper::toResponse) // Convierte cada Marca a MarcaResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    /**
     * Actualiza una marca existente.
     * 
     * Proceso:
     * 1. Verifica que la marca exista
     * 2. Preserva la fecha de unión original
     * 3. Actualiza los campos básicos usando el mapper
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID de la marca a actualizar
     * @param dto Nuevos datos de la marca
     * @return MarcaResponseDTO actualizado
     * @throws ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * PUT /api/v1/marcas/1
     * Body: { "nombreOficial": "EcoLife Actualizado", ... }
     */
    @Override
    @Transactional
    public MarcaResponseDTO updateMarca(Integer id, MarcaRequestDTO dto) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        Marca existingMarca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));

        // 2. Preservamos la fecha de unión original (es inmutable)
        // La fecha de unión no se actualiza, se mantiene con su valor original

        // 3. Actualizamos los campos básicos usando el mapper
        marcaMapper.updateEntityFromDto(existingMarca, dto);

        // 4. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Marca updatedMarca = marcaRepository.save(existingMarca);

        // 5. Convertir entidad a DTO de respuesta usando el mapper
        return marcaMapper.toResponse(updatedMarca);
    }

    /**
     * Elimina una marca de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que la marca exista
     * 2. Elimina la marca de la BD
     * 
     * Nota: Si la marca tiene productos asociados, la eliminación puede fallar
     * debido a restricciones de integridad referencial, a menos que se maneje
     * con CASCADE DELETE o eliminando primero los productos.
     * 
     * @param id Identificador de la marca a eliminar
     * @throws ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/marcas/1
     */
    @Override
    @Transactional
    public void deleteMarca(Integer id) {
        // Verificamos que la marca exista
        if (!marcaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Marca no encontrada con id: " + id);
        }
        
        // Eliminamos la marca de la BD
        marcaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public com.ecoshop.dto.Marca.MarcaPaginadoResponse buscarMarcasPorNombre(String query, Integer page, Integer size) {
        if (query == null || query.trim().isEmpty()) {
            // Retornar respuesta vacía paginada
            return com.ecoshop.dto.Marca.MarcaPaginadoResponse.builder()
                    .marcas(List.of())
                    .paginaActual(1)
                    .tamanoPagina(6)
                    .totalElementos(0L)
                    .totalPaginas(0)
                    .tieneSiguiente(false)
                    .tieneAnterior(false)
                    .esPrimera(true)
                    .esUltima(true)
                    .build();
        }
        
        // Valores por defecto: página 1, tamaño 6 (según requerimiento de UX/UI)
        // Convertir página basada en 1 a índice basado en 0 para cálculos internos
        int paginaUsuario = (page != null && page >= 1) ? page : 1;
        int paginaInterna = paginaUsuario - 1; // Convertir a índice basado en 0
        int tamano = (size != null && size > 0) ? size : 6;
        
        // Buscar marcas por nombre oficial (búsqueda parcial, case-insensitive)
        List<Marca> todasMarcas = marcaRepository.findByNombreOficialContainingIgnoreCase(query.trim());
        
        // Calcular metadatos de paginación
        long totalElementos = todasMarcas.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamano);
        int inicio = paginaInterna * tamano; // Usar índice basado en 0
        int fin = Math.min(inicio + tamano, todasMarcas.size());
        
        // Aplicar paginación manual
        List<Marca> marcasPaginadas = inicio < todasMarcas.size() 
                ? todasMarcas.subList(inicio, fin)
                : new ArrayList<>();
        
        // Convertir a DTOs
        List<MarcaResponseDTO> marcasDTO = marcasPaginadas.stream()
                .map(marcaMapper::toResponse)
                .collect(Collectors.toList());
        
        // Construir respuesta paginada (página basada en 1 para el usuario)
        return com.ecoshop.dto.Marca.MarcaPaginadoResponse.builder()
                .marcas(marcasDTO)
                .paginaActual(paginaUsuario) // Página basada en 1
                .tamanoPagina(tamano)
                .totalElementos(totalElementos)
                .totalPaginas(totalPaginas)
                .tieneSiguiente(paginaUsuario < totalPaginas)
                .tieneAnterior(paginaUsuario > 1)
                .esPrimera(paginaUsuario == 1)
                .esUltima(paginaUsuario >= totalPaginas)
                .build();
    }
}
