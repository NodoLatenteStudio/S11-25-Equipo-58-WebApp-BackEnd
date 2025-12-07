package com.ecoshop.service.contenido.impl;

import com.ecoshop.domain.ContenidoEducativo;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoRequestDTO;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoResponseDTO;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.ContenidoEducativoMapper;
import com.ecoshop.repository.contenido.ContenidoEducativoRepository;
import com.ecoshop.service.contenido.ContenidoEducativoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar contenido educativo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContenidoEducativoServiceImpl implements ContenidoEducativoService {

    private final ContenidoEducativoRepository contenidoEducativoRepository;
    private final ContenidoEducativoMapper contenidoEducativoMapper;

    @Override
    public ContenidoEducativoResponseDTO createContenidoEducativo(ContenidoEducativoRequestDTO dto) {
        ContenidoEducativo contenido = contenidoEducativoMapper.toEntity(dto);
        ContenidoEducativo savedContenido = contenidoEducativoRepository.save(contenido);
        return contenidoEducativoMapper.toResponseDTO(savedContenido);
    }

    @Override
    @Transactional(readOnly = true)
    public ContenidoEducativoResponseDTO getContenidoEducativoById(Integer id) {
        ContenidoEducativo contenido = contenidoEducativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido educativo no encontrado con id: " + id));
        return contenidoEducativoMapper.toResponseDTO(contenido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenidoEducativoResponseDTO> getAllContenidosEducativos() {
        return contenidoEducativoRepository.findAll().stream()
                .map(contenidoEducativoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenidoEducativoResponseDTO> getContenidosEducativosActivos() {
        return contenidoEducativoRepository.findByActivo(true).stream()
                .map(contenidoEducativoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenidoEducativoResponseDTO> getContenidosEducativosByCategoria(String categoria) {
        return contenidoEducativoRepository.findByCategoriaIgnoreCaseAndActivo(categoria, true).stream()
                .map(contenidoEducativoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenidoEducativoResponseDTO> buscarContenidosEducativosPorTitulo(String titulo) {
        return contenidoEducativoRepository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(contenidoEducativoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ContenidoEducativoResponseDTO updateContenidoEducativo(Integer id, ContenidoEducativoRequestDTO dto) {
        ContenidoEducativo contenido = contenidoEducativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido educativo no encontrado con id: " + id));
        
        contenidoEducativoMapper.updateEntityFromDTO(contenido, dto);
        ContenidoEducativo updatedContenido = contenidoEducativoRepository.save(contenido);
        return contenidoEducativoMapper.toResponseDTO(updatedContenido);
    }

    @Override
    public void deleteContenidoEducativo(Integer id) {
        ContenidoEducativo contenido = contenidoEducativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido educativo no encontrado con id: " + id));
        contenidoEducativoRepository.delete(contenido);
    }
}

