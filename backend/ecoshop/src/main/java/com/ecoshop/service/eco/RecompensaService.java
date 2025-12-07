package com.ecoshop.service.eco;

import com.ecoshop.dto.Recompensa.RecompensaRequestDTO;
import com.ecoshop.dto.Recompensa.RecompensaResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar recompensas del catálogo de Eco-Wallet.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre recompensas.
 */
public interface RecompensaService {

    /**
     * Crea una nueva recompensa en el catálogo.
     * 
     * @param dto Datos de la recompensa a crear
     * @return RecompensaResponseDTO con la recompensa creada
     */
    RecompensaResponseDTO createRecompensa(RecompensaRequestDTO dto);

    /**
     * Obtiene una recompensa por su ID.
     * 
     * @param id ID de la recompensa
     * @return RecompensaResponseDTO con los datos de la recompensa
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     */
    RecompensaResponseDTO getRecompensaById(Integer id);

    /**
     * Obtiene todas las recompensas.
     * 
     * @return Lista de todas las recompensas
     */
    List<RecompensaResponseDTO> getAllRecompensas();

    /**
     * Obtiene todas las recompensas activas.
     * 
     * @return Lista de recompensas activas
     */
    List<RecompensaResponseDTO> getRecompensasActivas();

    /**
     * Obtiene recompensas por tipo.
     * 
     * @param tipo Tipo de recompensa
     * @return Lista de recompensas del tipo especificado
     */
    List<RecompensaResponseDTO> getRecompensasByTipo(String tipo);

    /**
     * Obtiene recompensas disponibles según los puntos del usuario.
     * 
     * @param puntosMaximos Puntos máximos disponibles del usuario
     * @return Lista de recompensas que se pueden canjear con los puntos especificados
     */
    List<RecompensaResponseDTO> getRecompensasDisponiblesPorPuntos(Integer puntosMaximos);

    /**
     * Actualiza una recompensa existente.
     * 
     * @param id ID de la recompensa a actualizar
     * @param dto Nuevos datos de la recompensa
     * @return RecompensaResponseDTO con la recompensa actualizada
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     */
    RecompensaResponseDTO updateRecompensa(Integer id, RecompensaRequestDTO dto);

    /**
     * Elimina una recompensa.
     * 
     * @param id ID de la recompensa a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     */
    void deleteRecompensa(Integer id);
}

