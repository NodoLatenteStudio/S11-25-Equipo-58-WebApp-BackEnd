package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa un item del carrito de compras en la base de datos.
 * 
 * Esta clase mapea la tabla "Carrito_Items" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa un producto agregado al carrito con su cantidad.
 * 
 * Estructura de la tabla:
 * - carrito_item_id: Identificador único (clave primaria, auto-generado)
 * - carrito_id: Clave foránea a la tabla Carritos (obligatorio)
 * - producto_id: Clave foránea a la tabla productos (obligatorio)
 * - cantidad: Cantidad del producto en el carrito (obligatorio, mínimo 1)
 * - precio_unitario: Precio unitario del producto al momento de agregarlo (obligatorio, decimal)
 * 
 * Relaciones:
 * - @ManyToOne: Relación con Carrito (carrito_id)
 * - @ManyToOne: Relación con Producto (producto_id)
 * 
 * Notas importantes:
 * - La relación con Carrito es Many-to-One, lo que significa que cada item
 *   pertenece a un único carrito y cada carrito puede tener múltiples items.
 * - La relación con Producto es Many-to-One, lo que significa que cada item
 *   referencia a un único producto y cada producto puede estar en múltiples items.
 * - El precio_unitario se "congela" al momento de agregar el item al carrito,
 *   para que si el precio del producto cambia después, el carrito mantenga el precio original.
 * - Se usa @JsonIgnore en las relaciones para evitar referencias circulares
 *   durante la serialización JSON (el DTO manejará la exposición de los IDs).
 */
@Entity
@Table(name = "Carrito_Items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    /**
     * Identificador único del item del carrito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrito_item_id")
    private Integer carritoItemId;

    /**
     * Relación Many-to-One con Carrito.
     * 
     * Cada item pertenece a un único carrito.
     * La relación se almacena mediante la columna carrito_id en la tabla Carrito_Items.
     * 
     * @ManyToOne: Relación muchos-a-uno con la entidad Carrito.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Carrito_Items (carrito_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta manejará la exposición del carritoId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Carrito carrito;

    /**
     * Relación Many-to-One con Producto.
     * 
     * Cada item referencia a un único producto.
     * La relación se almacena mediante la columna producto_id en la tabla Carrito_Items.
     * 
     * @ManyToOne: Relación muchos-a-uno con la entidad Producto.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Carrito_Items (producto_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta manejará la exposición del productoId y datos del producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Producto producto;

    /**
     * Cantidad del producto en el carrito.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * Representa la cantidad de unidades del producto que se incluyen en el carrito.
     * Debe ser al menos 1.
     * 
     * Ejemplos: 1, 2, 5, 10
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento de agregarlo al carrito.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * IMPORTANTE: Este precio se "congela" al momento de agregar el item al carrito.
     * Si el precio del producto cambia después, este valor no se modifica, garantizando
     * que el carrito mantenga el precio original al momento de agregarlo.
     * 
     * Usamos BigDecimal en lugar de double para evitar problemas de precisión
     * con cálculos monetarios.
     * 
     * Ejemplos: 14990.00, 15990.50, 9999.99
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}

