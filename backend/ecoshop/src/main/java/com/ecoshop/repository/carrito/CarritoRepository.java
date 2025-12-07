package com.ecoshop.repository.carrito;

import com.ecoshop.domain.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a los datos de carritos en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 * 
 * Genéricos:
 * - Carrito: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (carrito_id)
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    /**
     * Busca el carrito de un usuario específico.
     * 
     * Como cada usuario tiene un único carrito (relación One-to-One),
     * este método retorna un Optional que puede estar vacío si el usuario
     * aún no tiene un carrito creado.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el carrito del usuario, o vacío si no existe
     */
    Optional<Carrito> findByUsuario_UsuarioId(Integer usuarioId);

    /**
     * Verifica si existe un carrito para un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsuario_UsuarioId(Integer usuarioId);

    /**
     * Elimina el carrito de un usuario específico.
     * 
     * Útil para limpiar el carrito cuando se completa una compra o se cancela.
     * 
     * @param usuarioId ID del usuario
     */
    void deleteByUsuario_UsuarioId(Integer usuarioId);
}

