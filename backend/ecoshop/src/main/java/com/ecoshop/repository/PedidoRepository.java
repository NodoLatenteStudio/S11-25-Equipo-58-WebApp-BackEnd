package com.ecoshop.repository;

import com.ecoshop.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a los datos de pedidos en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 * 
 * Ventajas de usar Spring Data JPA:
 * - No necesitamos escribir código SQL manualmente
 * - Spring genera automáticamente las consultas
 * - Métodos tipo-safe (tipado seguro)
 * - Facilita las pruebas unitarias
 * - Soporte para paginación y ordenamiento
 * 
 * Genéricos:
 * - Pedido: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (pedido_id)
 */
@Repository // Indica a Spring que esta interfaz es un repositorio (bean de Spring)
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    /**
     * Busca todos los pedidos de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de pedidos del usuario
     */
    List<Pedido> findByUsuario_UsuarioId(Integer usuarioId);
    
    /**
     * Busca todos los pedidos con un estado específico.
     * 
     * @param estado Estado del pedido (pendiente_pago, procesando, enviado, entregado, cancelado)
     * @return Lista de pedidos con el estado especificado
     */
    List<Pedido> findByEstado(String estado);
    
    /**
     * Busca todos los pedidos de un usuario con un estado específico.
     * 
     * @param usuarioId ID del usuario
     * @param estado Estado del pedido
     * @return Lista de pedidos del usuario con el estado especificado
     */
    List<Pedido> findByUsuario_UsuarioIdAndEstado(Integer usuarioId, String estado);
    
    /**
     * Busca todos los pedidos realizados en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de pedidos realizados en el rango de fechas
     */
    List<Pedido> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
