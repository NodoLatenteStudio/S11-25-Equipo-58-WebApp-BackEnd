package com.ecoshop.service.inventario;

import com.ecoshop.dto.Inventario.ActualizarStockRequestDTO;
import com.ecoshop.dto.Inventario.HistorialStockResponse;
import com.ecoshop.dto.Inventario.PrediccionDemandaResponse;
import com.ecoshop.dto.Inventario.StockBajoResponse;

import java.time.LocalDateTime;

/**
 * Interfaz del servicio de gestión de inventario avanzada.
 * 
 * Este servicio proporciona funcionalidades avanzadas para la gestión de inventario:
 * - Alertas de stock bajo
 * - Historial de cambios de stock
 * - Predicción de demanda
 * - Actualización de stock con registro automático
 */
public interface InventarioService {

    /**
     * Obtiene productos con stock bajo para una marca.
     * 
     * @param marcaId ID de la marca
     * @param umbralStock Umbral para considerar stock bajo (opcional, por defecto 10)
     * @return StockBajoResponse con productos que tienen stock bajo
     */
    StockBajoResponse obtenerProductosStockBajo(Integer marcaId, Integer umbralStock);

    /**
     * Obtiene el historial completo de cambios de stock de un producto.
     * 
     * @param productoId ID del producto
     * @return HistorialStockResponse con todos los registros de cambios
     */
    HistorialStockResponse obtenerHistorialStock(Integer productoId);

    /**
     * Obtiene el historial de cambios de stock de un producto en un rango de fechas.
     * 
     * @param productoId ID del producto
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return HistorialStockResponse con registros en el rango de fechas
     */
    HistorialStockResponse obtenerHistorialStockPorFecha(
            Integer productoId, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);

    /**
     * Actualiza el stock de un producto y registra el cambio en el historial.
     * 
     * @param productoId ID del producto
     * @param request Datos de actualización (nuevo stock, tipo de movimiento, motivo)
     * @param usuarioId ID del usuario que realiza el cambio (opcional)
     * @return Producto actualizado con el nuevo stock
     */
    com.ecoshop.dto.Producto.ProductoResponseDTO actualizarStock(
            Integer productoId, 
            ActualizarStockRequestDTO request, 
            Integer usuarioId);

    /**
     * Obtiene la predicción de demanda para un producto.
     * 
     * Analiza las ventas recientes y proporciona recomendaciones de reposición.
     * 
     * @param productoId ID del producto
     * @return PrediccionDemandaResponse con análisis y recomendaciones
     */
    PrediccionDemandaResponse obtenerPrediccionDemanda(Integer productoId);

    /**
     * Registra un cambio de stock en el historial.
     * 
     * Este método se llama automáticamente cuando cambia el stock,
     * pero también puede ser llamado manualmente para registrar cambios.
     * 
     * @param productoId ID del producto
     * @param stockAnterior Stock antes del cambio
     * @param stockNuevo Stock después del cambio
     * @param tipoMovimiento Tipo de movimiento (entrada, salida, ajuste, venta, etc.)
     * @param motivo Motivo del cambio (opcional)
     * @param usuarioId ID del usuario que realizó el cambio (opcional)
     * @param pedidoId ID del pedido relacionado (opcional)
     */
    void registrarCambioStock(
            Integer productoId,
            Integer stockAnterior,
            Integer stockNuevo,
            String tipoMovimiento,
            String motivo,
            Integer usuarioId,
            Integer pedidoId);
}

