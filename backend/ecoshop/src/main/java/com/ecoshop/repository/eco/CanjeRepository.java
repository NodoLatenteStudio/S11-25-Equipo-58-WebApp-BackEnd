package com.ecoshop.repository.eco;

import com.ecoshop.domain.Canje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Canje.
 * 
 * Proporciona métodos para acceder a los datos de canjes en la base de datos.
 * Spring Data JPA implementa automáticamente los métodos estándar (save, findById, findAll, etc.)
 */
@Repository
public interface CanjeRepository extends JpaRepository<Canje, Integer> {

    /**
     * Busca todos los canjes de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de canjes del usuario, ordenados por fecha más reciente primero
     */
    @Query("SELECT c FROM Canje c WHERE c.usuario.usuarioId = :usuarioId ORDER BY c.fechaCanje DESC")
    List<Canje> findByUsuario_UsuarioId(@Param("usuarioId") Integer usuarioId);

    /**
     * Busca canjes de un usuario por estado.
     * 
     * @param usuarioId ID del usuario
     * @param estado Estado del canje
     * @return Lista de canjes del usuario con el estado especificado
     */
    @Query("SELECT c FROM Canje c WHERE c.usuario.usuarioId = :usuarioId AND c.estado = :estado ORDER BY c.fechaCanje DESC")
    List<Canje> findByUsuario_UsuarioIdAndEstado(@Param("usuarioId") Integer usuarioId, @Param("estado") String estado);

    /**
     * Busca un canje por código de canje.
     * 
     * @param codigoCanje Código único del canje
     * @return Canje encontrado, si existe
     */
    Optional<Canje> findByCodigoCanje(String codigoCanje);

    /**
     * Busca canjes de un usuario en un rango de fechas.
     * 
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de canjes del usuario en el rango de fechas especificado
     */
    @Query("SELECT c FROM Canje c WHERE c.usuario.usuarioId = :usuarioId AND c.fechaCanje BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fechaCanje DESC")
    List<Canje> findByUsuario_UsuarioIdAndFechaCanjeBetween(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Cuenta el número de canjes de una recompensa específica.
     * 
     * @param recompensaId ID de la recompensa
     * @return Número de canjes de la recompensa
     */
    @Query("SELECT COUNT(c) FROM Canje c WHERE c.recompensa.recompensaId = :recompensaId")
    Long countByRecompensa_RecompensaId(@Param("recompensaId") Integer recompensaId);
}

