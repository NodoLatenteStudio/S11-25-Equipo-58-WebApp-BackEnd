package com.ecoshop.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA que representa un producto en la base de datos.
 * 
 * Esta clase mapea la tabla "productos" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa una fila en la tabla.
 * 
 * Estructura de la tabla según el esquema:
 * - producto_id: Identificador único (clave primaria, auto-generado)
 * - marca_id: Clave foránea a la tabla Marcas (obligatorio)
 * - nombre: Nombre del producto (obligatorio, máximo 200 caracteres)
 * - descripcion: Descripción del producto (opcional, TEXT)
 * - precio: Precio del producto (obligatorio, decimal)
 * - stock: Stock disponible del producto (obligatorio, default 0)
 * - sku: Código SKU único del producto (opcional, único, máximo 100 caracteres)
 * - materiales: Materiales utilizados en la fabricación (opcional, TEXT)
 * - origen: Origen del producto (opcional, máximo 100 caracteres)
 * - huella_carbono_total: Huella de carbono total en kg CO₂ (opcional, decimal)
 * - porcentaje_reciclable: Porcentaje de material reciclable (opcional, 0-100)
 * - eco_badge: Eco badge o certificación ecológica (opcional, máximo 15 caracteres)
 * - fecha_creacion: Fecha de creación del producto (obligatorio, no actualizable)
 * - activo: Indica si el producto está activo (obligatorio, default true)
 * - imagen_url: URL de la imagen del producto (opcional)
 * 
 * Relaciones:
 * - @ManyToOne: Relación con Marca (marca_id)
 * - @ManyToMany: Relación many-to-many con Certificacion a través de producto_certificaciones
 */
@Entity
@Table(name = "productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"marca", "certificaciones"})
@ToString(exclude = {"marca", "certificaciones"})
public class Producto {

    /**
     * Identificador único del producto.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Integer productoId;

    /**
     * Relación Many-to-One con Marca.
     * 
     * Cada producto pertenece a una marca.
     * La relación se almacena mediante la columna marca_id en la tabla productos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    /**
     * Nombre del producto.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(length = 200): Longitud máxima de 200 caracteres en la BD
     */
    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * Descripción del producto.
     * 
     * Campo opcional que permite almacenar descripciones largas.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Precio del producto.
     * 
     * Usamos BigDecimal en lugar de double para evitar problemas de precisión
     * con cálculos monetarios.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Stock disponible del producto.
     * 
     * Representa la cantidad de unidades disponibles en inventario.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    /**
     * Código SKU (Stock Keeping Unit) único del producto.
     * 
     * Identificador único para gestión de inventario.
     */
    @Column(length = 100, unique = true)
    private String sku;

    /**
     * Materiales utilizados en la fabricación del producto.
     * 
     * Ejemplos: "Plástico reciclado", "Algodón orgánico", "Bambú"
     */
    @Column(columnDefinition = "TEXT")
    private String materiales;

    /**
     * Origen del producto.
     * 
     * Indica el país o región de origen del producto.
     * Ejemplos: "Argentina", "Chile", "Local"
     */
    @Column(length = 100)
    private String origen;

    /**
     * Huella de carbono total del producto en kilogramos de CO₂.
     * 
     * Representa la cantidad total de emisiones de CO₂ asociadas al producto.
     * Según el esquema, este campo se llama "huella_carbono_total".
     */
    @Column(name = "huella_carbono_total", precision = 10, scale = 2)
    private BigDecimal huellaCarbonoTotal;

    /**
     * Porcentaje de material reciclable del producto.
     * 
     * Valor entre 0 y 100 que indica el porcentaje de material reciclable.
     */
    @Column(name = "porcentaje_reciclable")
    @Builder.Default
    private Integer porcentajeReciclable = 0;

    /**
     * Eco badge o certificación ecológica del producto.
     * 
     * Identificador del badge ecológico que posee el producto.
     * Ejemplos: "bajo_impacto", "medio_impacto", "neutro"
     * Según el esquema, máximo 15 caracteres.
     */
    @Column(name = "eco_badge", length = 15)
    private String ecoBadge;

    /**
     * URL de la imagen del producto.
     * 
     * Campo opcional que almacena la dirección de la imagen del producto.
     */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    /**
     * Fecha de creación del producto en el sistema.
     * 
     * Se asigna automáticamente cuando se crea el producto.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el producto está activo y disponible para venta.
     * 
     * true: El producto está activo y disponible
     * false: El producto está inactivo (no se muestra en el catálogo)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Certificaciones ambientales asociadas al producto.
     * 
     * @ManyToMany: Relación many-to-many con la entidad Certificacion.
     * Esta relación se almacena en una tabla intermedia llamada "producto_certificaciones".
     * 
     * Estructura de la tabla intermedia producto_certificaciones:
     * - producto_id: Clave foránea que referencia a productos.producto_id
     * - certificacion_id: Clave foránea que referencia a Certificaciones.certificacion_id
     * 
     * @JoinTable: Especifica el nombre de la tabla intermedia y las columnas
     * - name: Nombre de la tabla intermedia
     * - joinColumns: Columna que referencia a esta entidad (Producto)
     * - inverseJoinColumns: Columna que referencia a la entidad relacionada (Certificacion)
     * 
     * FetchType.LAZY: Carga las certificaciones solo cuando se accede a ellas.
     * Esto evita problemas de rendimiento y referencias circulares durante la serialización.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "producto_certificaciones",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "certificacion_id")
    )
    @Builder.Default
    private Set<Certificacion> certificaciones = new HashSet<>();
}
