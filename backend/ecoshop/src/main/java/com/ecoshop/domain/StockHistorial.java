package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un registro en el historial de cambios de stock.
 * 
 * Esta clase mapea la tabla "Stock_Historial" en la base de datos PostgreSQL.
 * Registra cada cambio en el stock de un producto para auditoría y análisis.
 * 
 * Estructura de la tabla:
 * - stock_historial_id: Identificador único (clave primaria, auto-generado)
 * - producto_id: Clave foránea a la tabla productos (obligatorio)
 * - stock_anterior: Stock antes del cambio (obligatorio)
 * - stock_nuevo: Stock después del cambio (obligatorio)
 * - cantidad_cambio: Diferencia de stock (positivo = aumento, negativo = disminución)
 * - tipo_movimiento: Tipo de movimiento (entrada, salida, ajuste, venta, etc.)
 * - motivo: Motivo del cambio (opcional)
 * - usuario_id: ID del usuario que realizó el cambio (opcional, para cambios manuales)
 * - pedido_id: ID del pedido relacionado (opcional, para cambios por ventas)
 * - fecha_cambio: Fecha y hora del cambio (auto-generada)
 * 
 * Relaciones:
 * - @ManyToOne: Relación con Producto (producto_id)
 * 
 * Notas importantes:
 * - Se crea un registro automáticamente cada vez que cambia el stock
 * - Permite auditoría completa de movimientos de inventario
 * - Útil para análisis de demanda y predicción
 */
@Entity
@Table(name = "Stock_Historial")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistorial {

    /**
     * Identificador único del registro de historial.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_historial_id")
    private Integer stockHistorialId;

    /**
     * Relación Many-to-One con Producto.
     * 
     * Cada registro de historial pertenece a un producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Producto producto;

    /**
     * Stock anterior antes del cambio.
     */
    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    /**
     * Stock nuevo después del cambio.
     */
    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    /**
     * Cantidad de cambio (stock_nuevo - stock_anterior).
     * 
     * Positivo = aumento de stock (entrada)
     * Negativo = disminución de stock (salida)
     */
    @Column(name = "cantidad_cambio", nullable = false)
    private Integer cantidadCambio;

    /**
     * Tipo de movimiento de stock.
     * 
     * Valores posibles:
     * - "entrada": Aumento de stock (compra, reposición)
     * - "salida": Disminución de stock (venta, pérdida)
     * - "ajuste": Ajuste manual de inventario
     * - "venta": Venta de producto (automático al crear pedido)
     * - "cancelacion": Cancelación de pedido (restaura stock)
     */
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    /**
     * Motivo del cambio de stock.
     * 
     * Campo opcional que describe el motivo del cambio.
     * Ejemplos: "Venta pedido #123", "Reposición de inventario", "Ajuste por inventario físico"
     */
    @Column(columnDefinition = "TEXT")
    private String motivo;

    /**
     * ID del usuario que realizó el cambio.
     * 
     * Opcional. Solo se usa para cambios manuales realizados por usuarios.
     * Para cambios automáticos (ventas), puede ser null.
     */
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /**
     * ID del pedido relacionado.
     * 
     * Opcional. Solo se usa cuando el cambio de stock está relacionado con un pedido.
     */
    @Column(name = "pedido_id")
    private Integer pedidoId;

    /**
     * Fecha y hora del cambio.
     * 
     * Se genera automáticamente cuando se crea el registro.
     */
    @CreationTimestamp
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;
}

