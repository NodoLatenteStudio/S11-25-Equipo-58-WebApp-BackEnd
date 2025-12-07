package com.ecoshop.service.eco.impl;

import com.ecoshop.domain.Recompensa;
import com.ecoshop.dto.Recompensa.RecompensaRequestDTO;
import com.ecoshop.dto.Recompensa.RecompensaResponseDTO;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.RecompensaMapper;
import com.ecoshop.repository.eco.RecompensaRepository;
import com.ecoshop.service.eco.RecompensaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de recompensas.
 * 
 * Esta clase contiene la lógica de negocio para gestionar recompensas del catálogo de Eco-Wallet.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecompensaServiceImpl implements RecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final RecompensaMapper recompensaMapper;

    @Override
    @Transactional
    public RecompensaResponseDTO createRecompensa(RecompensaRequestDTO dto) {
        // Convertir DTO a entidad
        Recompensa recompensa = recompensaMapper.toEntity(dto);

        // Guardar en la BD
        Recompensa savedRecompensa = recompensaRepository.save(recompensa);

        // Convertir a DTO de respuesta
        return recompensaMapper.toResponse(savedRecompensa);
    }

    @Override
    public RecompensaResponseDTO getRecompensaById(Integer id) {
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id: " + id));

        return recompensaMapper.toResponse(recompensa);
    }

    @Override
    public List<RecompensaResponseDTO> getAllRecompensas() {
        List<Recompensa> recompensas = recompensaRepository.findAll();
        return recompensas.stream()
                .map(recompensaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecompensaResponseDTO> getRecompensasActivas() {
        List<Recompensa> recompensas = recompensaRepository.findByActivoTrue();
        return recompensas.stream()
                .map(recompensaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecompensaResponseDTO> getRecompensasByTipo(String tipo) {
        List<Recompensa> recompensas = recompensaRepository.findByTipoAndActivoTrue(tipo);
        return recompensas.stream()
                .map(recompensaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecompensaResponseDTO> getRecompensasDisponiblesPorPuntos(Integer puntosMaximos) {
        List<Recompensa> recompensas = recompensaRepository.findRecompensasDisponiblesPorPuntos(puntosMaximos);
        return recompensas.stream()
                .map(recompensaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecompensaResponseDTO updateRecompensa(Integer id, RecompensaRequestDTO dto) {
        // Buscar la recompensa existente
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id: " + id));

        // Actualizar con los nuevos datos
        recompensaMapper.updateEntityFromDto(recompensa, dto);

        // Guardar cambios
        Recompensa updatedRecompensa = recompensaRepository.save(recompensa);

        // Convertir a DTO de respuesta
        return recompensaMapper.toResponse(updatedRecompensa);
    }

    @Override
    @Transactional
    public void deleteRecompensa(Integer id) {
        // Verificar que la recompensa existe
        if (!recompensaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recompensa no encontrada con id: " + id);
        }

        // Eliminar la recompensa
        recompensaRepository.deleteById(id);
    }
}

