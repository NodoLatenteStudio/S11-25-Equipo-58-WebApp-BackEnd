package com.ecoshop.repository.carrito;

import com.ecoshop.domain.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a los datos de items del carrito en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 * 
 * Genéricos:
 * - CarritoItem: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (carrito_item_id)
 */
@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {

    /**
     * Busca todos los items de un carrito específico.
     * 
     * @param carritoId ID del carrito
     * @return Lista de items del carrito
     */
    List<CarritoItem> findByCarrito_CarritoId(Integer carritoId);

    /**
     * Busca un item específico de un carrito y producto.
     * 
     * Útil para verificar si un producto ya está en el carrito antes de agregarlo.
     * 
     * @param carritoId ID del carrito
     * @param productoId ID del producto
     * @return Optional con el item si existe, o vacío si no existe
     */
    Optional<CarritoItem> findByCarrito_CarritoIdAndProducto_ProductoId(Integer carritoId, Integer productoId);

    /**
     * Verifica si existe un item con un carrito y producto específicos.
     * 
     * Útil para evitar duplicados o verificar si un producto ya está en el carrito.
     * 
     * @param carritoId ID del carrito
     * @param productoId ID del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByCarrito_CarritoIdAndProducto_ProductoId(Integer carritoId, Integer productoId);

    /**
     * Elimina todos los items de un carrito específico.
     * 
     * Útil para limpiar el carrito cuando se completa una compra o se vacía.
     * 
     * @param carritoId ID del carrito
     */
    void deleteByCarrito_CarritoId(Integer carritoId);

    /**
     * Busca todos los items de un producto específico en cualquier carrito.
     * 
     * Útil para analizar qué carritos incluyen un producto determinado.
     * 
     * @param productoId ID del producto
     * @return Lista de items que incluyen el producto
     */
    List<CarritoItem> findByProducto_ProductoId(Integer productoId);
}

