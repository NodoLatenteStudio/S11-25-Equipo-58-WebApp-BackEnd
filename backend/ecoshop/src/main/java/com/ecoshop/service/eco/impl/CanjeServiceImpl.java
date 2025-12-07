package com.ecoshop.service.eco.impl;

import com.ecoshop.domain.Canje;
import com.ecoshop.domain.Recompensa;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Canje.CanjeRequestDTO;
import com.ecoshop.dto.Canje.CanjeResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.CanjeMapper;
import com.ecoshop.repository.eco.CanjeRepository;
import com.ecoshop.repository.eco.RecompensaRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.eco.CanjeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de canjes.
 * 
 * Esta clase contiene la lógica de negocio para gestionar canjes de recompensas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CanjeServiceImpl implements CanjeService {

    private final CanjeRepository canjeRepository;
    private final RecompensaRepository recompensaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CanjeMapper canjeMapper;

    @Override
    @Transactional
    public CanjeResponseDTO realizarCanje(Integer usuarioId, CanjeRequestDTO dto) {
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        // Buscar la recompensa
        Recompensa recompensa = recompensaRepository.findById(dto.getRecompensaId())
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id: " + dto.getRecompensaId()));

        // Validar que la recompensa esté activa
        if (!recompensa.getActivo()) {
            throw new BadRequestException("La recompensa no está disponible para canje");
        }

        // Validar que el usuario tenga suficientes eco-puntos
        Integer puntosUsuario = usuario.getEcoPuntos() != null ? usuario.getEcoPuntos() : 0;
        if (puntosUsuario < recompensa.getPuntosRequeridos()) {
            throw new BadRequestException(
                    String.format("No tienes suficientes eco-puntos. Necesitas %d puntos y tienes %d",
                            recompensa.getPuntosRequeridos(), puntosUsuario));
        }

        // Validar stock disponible (si aplica)
        if (recompensa.getStockDisponible() != null && recompensa.getStockDisponible() <= 0) {
            throw new BadRequestException("La recompensa no tiene stock disponible");
        }

        // Crear el canje
        Canje canje = Canje.builder()
                .usuario(usuario)
                .recompensa(recompensa)
                .puntosUsados(recompensa.getPuntosRequeridos())
                .estado("pendiente")
                .codigoCanje(generarCodigoCanje())
                .build();

        // Guardar el canje
        Canje savedCanje = canjeRepository.save(canje);

        // Descontar los puntos del usuario
        usuario.setEcoPuntos(puntosUsuario - recompensa.getPuntosRequeridos());
        usuarioRepository.save(usuario);

        // Actualizar stock de la recompensa (si aplica)
        if (recompensa.getStockDisponible() != null) {
            recompensa.setStockDisponible(recompensa.getStockDisponible() - 1);
            recompensaRepository.save(recompensa);
        }

        // Retornar el canje creado
        return canjeMapper.toResponse(savedCanje);
    }

    @Override
    public CanjeResponseDTO getCanjeById(Integer id) {
        Canje canje = canjeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canje no encontrado con id: " + id));

        return canjeMapper.toResponse(canje);
    }

    @Override
    public List<CanjeResponseDTO> getCanjesByUsuario(Integer usuarioId) {
        List<Canje> canjes = canjeRepository.findByUsuario_UsuarioId(usuarioId);
        return canjes.stream()
                .map(canjeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CanjeResponseDTO> getCanjesByUsuarioAndEstado(Integer usuarioId, String estado) {
        List<Canje> canjes = canjeRepository.findByUsuario_UsuarioIdAndEstado(usuarioId, estado);
        return canjes.stream()
                .map(canjeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CanjeResponseDTO> getCanjesByUsuarioAndFecha(Integer usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Canje> canjes;
        
        // Si ambos parámetros son null, retornar todos los canjes del usuario
        if (fechaInicio == null && fechaFin == null) {
            canjes = canjeRepository.findByUsuario_UsuarioId(usuarioId);
        } 
        // Si solo fechaInicio es null, usar fecha mínima
        else if (fechaInicio == null) {
            fechaInicio = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            canjes = canjeRepository.findByUsuario_UsuarioIdAndFechaCanjeBetween(usuarioId, fechaInicio, fechaFin);
        }
        // Si solo fechaFin es null, usar fecha actual
        else if (fechaFin == null) {
            fechaFin = LocalDateTime.now();
            canjes = canjeRepository.findByUsuario_UsuarioIdAndFechaCanjeBetween(usuarioId, fechaInicio, fechaFin);
        }
        // Ambos parámetros están presentes
        else {
            canjes = canjeRepository.findByUsuario_UsuarioIdAndFechaCanjeBetween(usuarioId, fechaInicio, fechaFin);
        }
        
        return canjes.stream()
                .map(canjeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CanjeResponseDTO actualizarEstadoCanje(Integer canjeId, String nuevoEstado) {
        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> new ResourceNotFoundException("Canje no encontrado con id: " + canjeId));

        // Validar estado válido
        if (!esEstadoValido(nuevoEstado)) {
            throw new BadRequestException("Estado inválido: " + nuevoEstado);
        }

        canje.setEstado(nuevoEstado);
        Canje updatedCanje = canjeRepository.save(canje);

        return canjeMapper.toResponse(updatedCanje);
    }

    /**
     * Genera un código único para el canje.
     * 
     * @return Código único generado
     */
    private String generarCodigoCanje() {
        return "CANJE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Valida que el estado sea válido.
     * 
     * @param estado Estado a validar
     * @return true si el estado es válido, false en caso contrario
     */
    private boolean esEstadoValido(String estado) {
        return estado != null && (
                estado.equals("pendiente") ||
                estado.equals("completado") ||
                estado.equals("cancelado") ||
                estado.equals("expirado")
        );
    }
}

