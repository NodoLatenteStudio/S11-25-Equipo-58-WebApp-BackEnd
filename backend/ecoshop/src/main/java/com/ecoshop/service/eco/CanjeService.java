package com.ecoshop.service.eco;

import com.ecoshop.dto.Canje.CanjeRequestDTO;
import com.ecoshop.dto.Canje.CanjeResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz del servicio para gestionar canjes de recompensas.
 * 
 * Este servicio se encarga de procesar canjes de recompensas usando eco-puntos.
 */
public interface CanjeService {

    /**
     * Realiza un canje de recompensa para un usuario.
     * 
     * Valida que:
     * - La recompensa exista y esté activa
     * - El usuario tenga suficientes eco-puntos
     * - Haya stock disponible (si aplica)
     * 
     * Descuenta los puntos del usuario y crea el registro de canje.
     * 
     * @param usuarioId ID del usuario que realiza el canje
     * @param dto Datos del canje (recompensaId)
     * @return CanjeResponseDTO con los datos del canje realizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     * @throws com.ecoshop.exception.BadRequestException si no hay suficientes puntos o stock
     */
    CanjeResponseDTO realizarCanje(Integer usuarioId, CanjeRequestDTO dto);

    /**
     * Obtiene un canje por su ID.
     * 
     * @param id ID del canje
     * @return CanjeResponseDTO con los datos del canje
     * @throws com.ecoshop.exception.ResourceNotFoundException si el canje no existe
     */
    CanjeResponseDTO getCanjeById(Integer id);

    /**
     * Obtiene todos los canjes de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de canjes del usuario, ordenados por fecha más reciente primero
     */
    List<CanjeResponseDTO> getCanjesByUsuario(Integer usuarioId);

    /**
     * Obtiene canjes de un usuario por estado.
     * 
     * @param usuarioId ID del usuario
     * @param estado Estado del canje (pendiente, completado, cancelado, expirado)
     * @return Lista de canjes del usuario con el estado especificado
     */
    List<CanjeResponseDTO> getCanjesByUsuarioAndEstado(Integer usuarioId, String estado);

    /**
     * Obtiene canjes de un usuario en un rango de fechas.
     * 
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de canjes del usuario en el rango de fechas
     */
    List<CanjeResponseDTO> getCanjesByUsuarioAndFecha(Integer usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Actualiza el estado de un canje.
     * 
     * @param canjeId ID del canje
     * @param nuevoEstado Nuevo estado del canje
     * @return CanjeResponseDTO con el canje actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el canje no existe
     */
    CanjeResponseDTO actualizarEstadoCanje(Integer canjeId, String nuevoEstado);
}
