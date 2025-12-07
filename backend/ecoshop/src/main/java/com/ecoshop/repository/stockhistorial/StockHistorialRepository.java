package com.ecoshop.repository.stockhistorial;

import com.ecoshop.domain.StockHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a los datos del historial de stock en la base de datos.
 */
@Repository
public interface StockHistorialRepository extends JpaRepository<StockHistorial, Integer> {

    /**
     * Busca todos los registros de historial de un producto, ordenados por fecha descendente.
     * 
     * @param productoId ID del producto
     * @return Lista de registros de historial del producto
     */
    @Query("SELECT sh FROM StockHistorial sh WHERE sh.producto.productoId = :productoId ORDER BY sh.fechaCambio DESC")
    List<StockHistorial> findByProducto_ProductoIdOrderByFechaCambioDesc(@Param("productoId") Integer productoId);

    /**
     * Busca registros de historial de un producto en un rango de fechas.
     * 
     * @param productoId ID del producto
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de registros de historial en el rango de fechas
     */
    @Query("SELECT sh FROM StockHistorial sh WHERE sh.producto.productoId = :productoId " +
           "AND sh.fechaCambio BETWEEN :fechaInicio AND :fechaFin ORDER BY sh.fechaCambio DESC")
    List<StockHistorial> findByProducto_ProductoIdAndFechaCambioBetween(
            @Param("productoId") Integer productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca registros de historial por tipo de movimiento.
     * 
     * @param productoId ID del producto
     * @param tipoMovimiento Tipo de movimiento (entrada, salida, ajuste, venta, etc.)
     * @return Lista de registros de historial con el tipo de movimiento especificado
     */
    @Query("SELECT sh FROM StockHistorial sh WHERE sh.producto.productoId = :productoId " +
           "AND sh.tipoMovimiento = :tipoMovimiento ORDER BY sh.fechaCambio DESC")
    List<StockHistorial> findByProducto_ProductoIdAndTipoMovimiento(
            @Param("productoId") Integer productoId,
            @Param("tipoMovimiento") String tipoMovimiento);

    /**
     * Cuenta las ventas (salidas) de un producto en un período de tiempo.
     * 
     * @param productoId ID del producto
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Cantidad total de unidades vendidas (suma de cantidadCambio negativo)
     */
    @Query("SELECT COALESCE(SUM(ABS(sh.cantidadCambio)), 0) FROM StockHistorial sh " +
           "WHERE sh.producto.productoId = :productoId " +
           "AND sh.tipoMovimiento = 'venta' " +
           "AND sh.fechaCambio BETWEEN :fechaInicio AND :fechaFin")
    Long contarVentasEnPeriodo(
            @Param("productoId") Integer productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}

