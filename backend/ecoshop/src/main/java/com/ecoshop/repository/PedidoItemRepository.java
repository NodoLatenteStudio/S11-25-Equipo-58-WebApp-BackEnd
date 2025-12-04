package com.ecoshop.repository;

import com.ecoshop.domain.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a los datos de items de pedido en la base de datos.
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
 * - PedidoItem: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (pedido_item_id)
 */
@Repository // Indica a Spring que esta interfaz es un repositorio (bean de Spring)
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Integer> {

    /**
     * Busca todos los items de un pedido específico.
     * 
     * @param pedidoId ID del pedido
     * @return Lista de items del pedido
     */
    List<PedidoItem> findByPedido_PedidoId(Integer pedidoId);
    
    /**
     * Busca todos los items de un producto específico.
     * 
     * Útil para analizar qué pedidos incluyen un producto determinado.
     * 
     * @param productoId ID del producto
     * @return Lista de items que incluyen el producto
     */
    List<PedidoItem> findByProducto_ProductoId(Integer productoId);
    
    /**
     * Busca todos los items de un pedido con una cantidad específica.
     * 
     * @param pedidoId ID del pedido
     * @param cantidad Cantidad a buscar
     * @return Lista de items del pedido con la cantidad especificada
     */
    List<PedidoItem> findByPedido_PedidoIdAndCantidad(Integer pedidoId, Integer cantidad);
    
    /**
     * Verifica si existe un item con un pedido y producto específicos.
     * 
     * Útil para evitar duplicados o verificar si un producto ya está en el pedido.
     * 
     * @param pedidoId ID del pedido
     * @param productoId ID del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByPedido_PedidoIdAndProducto_ProductoId(Integer pedidoId, Integer productoId);
}
