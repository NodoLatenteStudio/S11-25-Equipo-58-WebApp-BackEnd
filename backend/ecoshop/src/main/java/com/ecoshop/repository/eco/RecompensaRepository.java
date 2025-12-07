package com.ecoshop.repository.eco;

import com.ecoshop.domain.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Recompensa.
 * 
 * Proporciona métodos para acceder a los datos de recompensas en la base de datos.
 * Spring Data JPA implementa automáticamente los métodos estándar (save, findById, findAll, etc.)
 */
@Repository
public interface RecompensaRepository extends JpaRepository<Recompensa, Integer> {

    /**
     * Busca todas las recompensas activas.
     * 
     * @return Lista de recompensas activas
     */
    List<Recompensa> findByActivoTrue();

    /**
     * Busca recompensas por tipo.
     * 
     * @param tipo Tipo de recompensa
     * @return Lista de recompensas del tipo especificado
     */
    List<Recompensa> findByTipo(String tipo);

    /**
     * Busca recompensas activas por tipo.
     * 
     * @param tipo Tipo de recompensa
     * @return Lista de recompensas activas del tipo especificado
     */
    List<Recompensa> findByTipoAndActivoTrue(String tipo);

    /**
     * Busca recompensas cuyo costo en puntos sea menor o igual al valor especificado.
     * 
     * @param puntosMaximos Puntos máximos disponibles
     * @return Lista de recompensas que se pueden canjear con los puntos especificados
     */
    @Query("SELECT r FROM Recompensa r WHERE r.activo = true AND r.puntosRequeridos <= :puntosMaximos ORDER BY r.puntosRequeridos ASC")
    List<Recompensa> findRecompensasDisponiblesPorPuntos(Integer puntosMaximos);

    /**
     * Busca una recompensa por código de canje (si aplica).
     * 
     * Nota: Este método busca en la tabla Canjes, no en Recompensas directamente.
     * Se incluye aquí por completitud, pero la búsqueda real se hace en CanjeRepository.
     */
    Optional<Recompensa> findByRecompensaId(Integer recompensaId);
}

