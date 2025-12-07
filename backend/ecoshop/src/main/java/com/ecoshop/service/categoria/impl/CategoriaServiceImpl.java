package com.ecoshop.service.categoria.impl;

import com.ecoshop.domain.Categoria;
import com.ecoshop.dto.Categoria.CategoriaRequestDTO;
import com.ecoshop.dto.Categoria.CategoriaResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.CategoriaMapper;
import com.ecoshop.repository.categoria.CategoriaRepository;
import com.ecoshop.service.categoria.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de categorías.
 * 
 * Esta clase contiene la lógica de negocio para gestionar categorías.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    @Transactional
    public CategoriaResponseDTO createCategoria(CategoriaRequestDTO dto) {
        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Convertir DTO a entidad
        Categoria categoria = categoriaMapper.toEntity(dto);

        // Guardar en la BD
        Categoria savedCategoria = categoriaRepository.save(categoria);

        // Convertir a DTO de respuesta
        return categoriaMapper.toResponse(savedCategoria);
    }

    @Override
    public CategoriaResponseDTO getCategoriaById(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        return categoriaMapper.toResponse(categoria);
    }

    @Override
    public List<CategoriaResponseDTO> getAllCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        
        return categorias.stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaResponseDTO updateCategoria(Integer id, CategoriaRequestDTO dto) {
        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Validar que si se cambia el nombre, no exista otra categoría con ese nombre
        if (dto.getNombre() != null && !dto.getNombre().equals(categoria.getNombre())) {
            if (categoriaRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe una categoría con el nombre: " + dto.getNombre());
            }
        }

        // Actualizar la entidad
        categoriaMapper.updateEntityFromDto(categoria, dto);

        // Guardar cambios
        Categoria updatedCategoria = categoriaRepository.save(categoria);

        // Convertir a DTO de respuesta
        return categoriaMapper.toResponse(updatedCategoria);
    }

    @Override
    @Transactional
    public void deleteCategoria(Integer id) {
        // Verificar que la categoría existe
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con id: " + id);
        }

        // Eliminar la categoría
        categoriaRepository.deleteById(id);
    }
}

