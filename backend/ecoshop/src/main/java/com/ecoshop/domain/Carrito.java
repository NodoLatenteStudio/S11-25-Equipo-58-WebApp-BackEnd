package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un carrito de compras en la base de datos.
 * 
 * Esta clase mapea la tabla "Carritos" en la base de datos PostgreSQL.
 * Cada usuario tiene un único carrito activo que persiste incluso si no completa la compra.
 * 
 * Estructura de la tabla:
 * - carrito_id: Identificador único (clave primaria, auto-generado)
 * - usuario_id: Clave foránea a la tabla Usuarios (obligatorio, único)
 * - total: Total del carrito en moneda (calculado automáticamente)
 * - fecha_creacion: Fecha en que se creó el carrito (auto-generada)
 * - fecha_actualizacion: Fecha de última actualización (auto-actualizada)
 * 
 * Relaciones:
 * - @OneToOne: Relación uno-a-uno con Usuario (cada usuario tiene un carrito)
 * - Relación inversa con CarritoItem (cada carrito tiene múltiples items)
 * 
 * Notas importantes:
 * - El carrito persiste en la base de datos para usuarios autenticados
 * - Si el usuario no está autenticado, el frontend usará localStorage
 * - El total se calcula automáticamente a partir de los CarritoItems asociados
 * - Se usa @JsonIgnore en la relación con Usuario para evitar referencias circulares
 */
@Entity
@Table(name = "Carritos", uniqueConstraints = {
    @UniqueConstraint(columnNames = "usuario_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {

    /**
     * Identificador único del carrito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrito_id")
    private Integer carritoId;

    /**
     * Relación One-to-One con Usuario.
     * 
     * Cada usuario tiene un único carrito activo.
     * La relación se almacena mediante la columna usuario_id en la tabla Carritos.
     * 
     * @OneToOne: Relación uno-a-uno con la entidad Usuario.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Carritos (usuario_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * - unique = true: Garantiza que cada usuario tenga solo un carrito
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Usuario usuario;

    /**
     * Total del carrito en moneda.
     * 
     * Se calcula automáticamente sumando el total de todos los CarritoItems.
     * Se inicializa en 0.00 cuando el carrito está vacío.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Fecha en que se creó el carrito.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha actual al crear la entidad
     * @Column(updatable = false): Este campo no se puede actualizar después de la creación
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización del carrito.
     * 
     * @UpdateTimestamp: Hibernate actualiza automáticamente este campo cada vez que se modifica la entidad
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

