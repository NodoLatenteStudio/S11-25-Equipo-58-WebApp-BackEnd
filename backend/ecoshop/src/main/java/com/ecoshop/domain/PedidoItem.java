package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa un item de pedido en la base de datos.
 * 
 * Esta clase mapea la tabla "Pedido_Items" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa una fila en la tabla.
 * 
 * Estructura de la tabla según el esquema:
 * - pedido_item_id: Identificador único (clave primaria, auto-generado)
 * - pedido_id: Clave foránea a la tabla Pedidos (obligatorio)
 * - producto_id: Clave foránea a la tabla productos (obligatorio)
 * - cantidad: Cantidad del producto en el pedido (obligatorio, mínimo 1)
 * - precio_unitario: Precio unitario del producto al momento de la compra (obligatorio, decimal)
 * 
 * Relaciones:
 * - @ManyToOne: Relación con Pedido (pedido_id)
 * - @ManyToOne: Relación con Producto (producto_id)
 * 
 * Notas importantes:
 * - La relación con Pedido es Many-to-One, lo que significa que cada item
 *   pertenece a un único pedido y cada pedido puede tener múltiples items.
 * - La relación con Producto es Many-to-One, lo que significa que cada item
 *   referencia a un único producto y cada producto puede estar en múltiples items.
 * - El precio_unitario se "congela" al momento de agregar el item al pedido,
 *   para que si el precio del producto cambia después, el pedido mantenga el precio original.
 * - Se usa @JsonIgnore en las relaciones para evitar referencias circulares
 *   durante la serialización JSON (el DTO manejará la exposición de los IDs).
 */
@Entity
@Table(name = "Pedido_Items")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class PedidoItem {

    /**
     * Identificador único del item de pedido.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pedido_item_id")
    private Integer pedidoItemId;

    /**
     * Relación Many-to-One con Pedido.
     * 
     * Cada item pertenece a un único pedido.
     * La relación se almacena mediante la columna pedido_id en la tabla Pedido_Items.
     * 
     * @ManyToOne: Relación muchos-a-uno con la entidad Pedido.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Pedido_Items (pedido_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta (PedidoItemResponseDTO) manejará la exposición del pedidoId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore // Evita referencias circulares durante la serialización JSON
    @EqualsAndHashCode.Exclude // Excluye pedido del equals y hashCode para evitar referencias circulares
    @ToString.Exclude // Excluye pedido del toString para evitar referencias circulares
    private Pedido pedido;

    /**
     * Relación Many-to-One con Producto.
     * 
     * Cada item referencia a un único producto.
     * La relación se almacena mediante la columna producto_id en la tabla Pedido_Items.
     * 
     * @ManyToOne: Relación muchos-a-uno con la entidad Producto.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Pedido_Items (producto_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta (PedidoItemResponseDTO) manejará la exposición del productoId y datos del producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore // Evita referencias circulares durante la serialización JSON
    @EqualsAndHashCode.Exclude // Excluye producto del equals y hashCode para evitar referencias circulares
    @ToString.Exclude // Excluye producto del toString para evitar referencias circulares
    private Producto producto;

    /**
     * Cantidad del producto en el pedido.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * Representa la cantidad de unidades del producto que se incluyen en el pedido.
     * Debe ser al menos 1.
     * 
     * Ejemplos: 1, 2, 5, 10
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento de agregarlo al pedido.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * IMPORTANTE: Este precio se "congela" al momento de agregar el item al pedido.
     * Si el precio del producto cambia después, este valor no se modifica, garantizando
     * que el pedido mantenga el precio original al momento de la compra.
     * 
     * Usamos BigDecimal en lugar de double para evitar problemas de precisión
     * con cálculos monetarios.
     * 
     * Ejemplos: 14990.00, 15990.50, 9999.99
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}
