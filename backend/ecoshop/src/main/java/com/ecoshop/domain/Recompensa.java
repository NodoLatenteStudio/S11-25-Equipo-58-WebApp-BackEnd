package com.ecoshop.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una recompensa en el catálogo de Eco-Wallet.
 * 
 * Esta clase mapea la tabla "Recompensas" en la base de datos PostgreSQL.
 * Las recompensas son premios que los usuarios pueden canjear usando sus eco-puntos.
 * 
 * Estructura de la tabla:
 * - recompensa_id: Identificador único (clave primaria, auto-generado)
 * - nombre: Nombre de la recompensa (obligatorio, máximo 200 caracteres)
 * - descripcion: Descripción detallada de la recompensa (opcional, TEXT)
 * - puntos_requeridos: Cantidad de eco-puntos necesarios para canjear (obligatorio)
 * - tipo: Tipo de recompensa (obligatorio, máximo 50 caracteres)
 * - valor: Valor monetario o equivalente de la recompensa (opcional, decimal)
 * - stock_disponible: Cantidad disponible de la recompensa (opcional, null = ilimitado)
 * - imagen_url: URL de la imagen de la recompensa (opcional)
 * - activo: Indica si la recompensa está disponible para canje (obligatorio, default true)
 * - fecha_creacion: Fecha de creación (obligatorio, no actualizable)
 * - fecha_actualizacion: Fecha de última actualización (obligatorio)
 */
@Entity
@Table(name = "Recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    /**
     * Identificador único de la recompensa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recompensa_id")
    private Integer recompensaId;

    /**
     * Nombre de la recompensa.
     * 
     * Ejemplos: "Descuento 10%", "Envío gratis", "Producto ecológico gratis"
     */
    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * Descripción detallada de la recompensa.
     * 
     * Proporciona información adicional sobre qué incluye la recompensa
     * y cómo se puede usar.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Cantidad de eco-puntos necesarios para canjear esta recompensa.
     * 
     * Debe ser un valor positivo.
     */
    @Column(name = "puntos_requeridos", nullable = false)
    private Integer puntosRequeridos;

    /**
     * Tipo de recompensa.
     * 
     * Ejemplos: "descuento", "envio_gratis", "producto", "donacion", "experiencia"
     */
    @Column(nullable = false, length = 50)
    private String tipo;

    /**
     * Valor monetario o equivalente de la recompensa.
     * 
     * Útil para recompensas con valor monetario (descuentos, productos, etc.)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    /**
     * Cantidad disponible de la recompensa.
     * 
     * null = ilimitado
     * Si tiene un valor, representa la cantidad restante disponible.
     */
    @Column(name = "stock_disponible")
    private Integer stockDisponible;

    /**
     * URL de la imagen de la recompensa.
     * 
     * Útil para mostrar la recompensa en el catálogo del frontend.
     */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    /**
     * Indica si la recompensa está activa y disponible para canje.
     * 
     * true: La recompensa está disponible
     * false: La recompensa está desactivada (no se puede canjear)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Fecha de creación de la recompensa.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización de la recompensa.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

