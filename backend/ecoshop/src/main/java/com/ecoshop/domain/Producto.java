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
     * - categoria_id: Clave foránea a la tabla Categorias (opcional)
     * - nombre: Nombre del producto (obligatorio, máximo 200 caracteres)
     * - descripcion: Descripción del producto (opcional, TEXT)
     * - precio: Precio del producto (obligatorio, decimal)
     * - stock: Stock disponible del producto (obligatorio, default 0)
     * - sku: Código SKU único del producto (opcional, único, máximo 100 caracteres)
     * - materiales: Materiales utilizados en la fabricación (opcional, TEXT)
     * - origen: Origen del producto (opcional, máximo 100 caracteres)
     * - origen_pais: País de origen del producto (opcional, máximo 100 caracteres)
     * - huella_carbono_total: Huella de carbono total en kg CO₂ (opcional, decimal)
     * - porcentaje_reciclable: Porcentaje de material reciclable (opcional, 0-100)
     * - eco_badge: Eco badge o certificación ecológica (opcional, máximo 15 caracteres)
     * - consumo_agua: Consumo de agua en litros durante la producción (opcional)
     * - distancia_transporte: Distancia de transporte en kilómetros (opcional)
     * - co2_ahorrado_vs_convencional: CO₂ ahorrado comparado con producto convencional (opcional, decimal)
     * - emisiones_fabricacion: Emisiones de CO₂ por fabricación (opcional, decimal)
     * - emisiones_empaque: Emisiones de CO₂ por empaque (opcional, decimal)
     * - emisiones_transporte: Emisiones de CO₂ por transporte (opcional, decimal)
     * - emisiones_entrega: Emisiones de CO₂ por entrega (última milla) (opcional, decimal)
     * - logistica_optimizada: Indica si la logística está optimizada (opcional, default false)
     * - ultima_milla_carbono_neutral: Indica si la entrega es carbono neutral (opcional, default false)
     * - fecha_creacion: Fecha de creación del producto (obligatorio, no actualizable)
     * - activo: Indica si el producto está activo (obligatorio, default true)
     * - imagen_url: URL de la imagen del producto (opcional)
     * 
     * Relaciones:
     * - @ManyToOne: Relación con Marca (marca_id)
     * - @ManyToOne: Relación con Categoria (categoria_id) - opcional
     * - @ManyToMany: Relación many-to-many con Certificacion a través de producto_certificaciones
     */
@Entity
@Table(name = "productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"marca", "categoria", "certificaciones"})
@ToString(exclude = {"marca", "categoria", "certificaciones"})
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
     * Relación Many-to-One con Categoria.
     * 
     * Cada producto pertenece a una categoría.
     * La relación se almacena mediante la columna categoria_id en la tabla productos.
     * 
     * Campo opcional: un producto puede no tener categoría asignada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

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
     * País de origen del producto.
     * 
     * Indica específicamente el país de fabricación/origen.
     * Ejemplos: "Argentina", "Chile", "Brasil", "Colombia"
     * 
     * Nota: Este campo es más específico que "origen" que puede incluir regiones.
     */
    @Column(name = "origen_pais", length = 100)
    private String origenPais;

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
     * Consumo de agua en litros durante la producción del producto.
     * 
     * Representa la cantidad de agua consumida en el proceso de fabricación.
     * Ejemplos: 50, 100, 250 litros
     */
    @Column(name = "consumo_agua")
    private Integer consumoAgua;

    /**
     * Distancia de transporte en kilómetros desde el origen.
     * 
     * Representa la distancia estimada desde el lugar de origen/fabricación
     * hasta el punto de distribución o venta.
     * Ejemplos: 50, 1000, 5000 km
     */
    @Column(name = "distancia_transporte")
    private Integer distanciaTransporte;

    /**
     * CO₂ ahorrado en kilogramos comparado con un producto convencional equivalente.
     * 
     * Representa la diferencia de emisiones entre este producto sostenible
     * y un producto convencional similar.
     */
    @Column(name = "co2_ahorrado_vs_convencional", precision = 10, scale = 2)
    private BigDecimal co2AhorradoVsConvencional;

    /**
     * Emisiones de CO₂ por fabricación del producto en kilogramos.
     * 
     * Representa las emisiones generadas durante el proceso de fabricación.
     */
    @Column(name = "emisiones_fabricacion", precision = 10, scale = 2)
    private BigDecimal emisionesFabricacion;

    /**
     * Emisiones de CO₂ por empaque del producto en kilogramos.
     * 
     * Representa las emisiones generadas por el empaque del producto.
     */
    @Column(name = "emisiones_empaque", precision = 10, scale = 2)
    private BigDecimal emisionesEmpaque;

    /**
     * Emisiones de CO₂ por transporte del producto en kilogramos.
     * 
     * Representa las emisiones generadas durante el transporte desde el origen.
     */
    @Column(name = "emisiones_transporte", precision = 10, scale = 2)
    private BigDecimal emisionesTransporte;

    /**
     * Emisiones de CO₂ por entrega (última milla) del producto en kilogramos.
     * 
     * Representa las emisiones generadas durante la entrega final al cliente.
     */
    @Column(name = "emisiones_entrega", precision = 10, scale = 2)
    private BigDecimal emisionesEntrega;

    /**
     * Indica si la logística de transporte está optimizada.
     * 
     * true: La logística está optimizada (rutas eficientes, carga consolidada, etc.)
     * false: Logística estándar
     * 
     * Útil para calcular emisiones de transporte más precisas.
     */
    @Column(name = "logistica_optimizada")
    @Builder.Default
    private Boolean logisticaOptimizada = false;

    /**
     * Indica si la última milla (entrega final) es carbono neutral.
     * 
     * true: La entrega final es carbono neutral (compensación de emisiones, vehículos eléctricos, etc.)
     * false: Entrega estándar
     * 
     * Útil para calcular emisiones de entrega más precisas.
     */
    @Column(name = "ultima_milla_carbono_neutral")
    @Builder.Default
    private Boolean ultimaMillaCarbonoNeutral = false;

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
