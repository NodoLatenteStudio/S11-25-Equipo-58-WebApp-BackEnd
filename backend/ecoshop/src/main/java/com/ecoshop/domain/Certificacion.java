package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA que representa una certificación ambiental en la base de datos.
 * 
 * Esta clase mapea la tabla "Certificaciones" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa una certificación ambiental que puede
 * estar asociada a múltiples productos.
 * 
 * Estructura de la tabla según el esquema:
 * - certificacion_id: Identificador único (clave primaria, auto-generado)
 * - nombre_sello: Nombre del sello de certificación (obligatorio, varchar)
 * - descripcion: Descripción de la certificación (opcional, text)
 * - entidad_emisora: Entidad que emite la certificación (opcional, varchar)
 * 
 * Relaciones:
 * - @ManyToMany: Relación many-to-many con Producto a través de la tabla intermedia
 *   "producto_certificaciones"
 *   - Esta relación está mapeada desde el lado de Producto (owner side)
 *   - Aquí solo se declara para mantener la bidireccionalidad
 *   - La tabla intermedia tiene: producto_id y certificacion_id
 * 
 * Notas importantes:
 * - La relación con productos es lazy para evitar problemas de rendimiento
 * - Los productos se excluyen del equals, hashCode y toString para evitar referencias circulares
 */
@Entity
@Table(name = "Certificaciones")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class Certificacion {

    /**
     * Identificador único de la certificación.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     * - El ID se asigna automáticamente cuando se guarda por primera vez
     */
    @Id // Indica que este campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // La BD genera el ID automáticamente
    @Column(name = "certificacion_id")
    private Integer certificacionId;

    /**
     * Nombre del sello de certificación.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * Ejemplos: "Fair Trade", "Carbon Neutral", "Organic", "Comercio Justo"
     */
    @Column(name = "nombre_sello", nullable = false)
    private String nombreSello;

    /**
     * Descripción de la certificación.
     * 
     * Campo opcional que permite proporcionar información detallada sobre la certificación.
     * 
     * Ejemplos: "Certificación que garantiza condiciones de comercio justo para productores",
     * "Certificación que verifica la neutralidad de carbono del producto"
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Entidad que emite la certificación.
     * 
     * Campo opcional que indica qué organización o entidad es responsable
     * de emitir y verificar esta certificación.
     * 
     * Ejemplos: "Fair Trade International", "Carbon Trust", "USDA Organic"
     */
    @Column(name = "entidad_emisora")
    private String entidadEmisora;

    /**
     * Productos que tienen esta certificación.
     * 
     * @ManyToMany: Relación many-to-many con la entidad Producto.
     * Esta relación está mapeada desde el lado de Producto (owner side), por lo que
     * aquí solo la declaramos para mantener la bidireccionalidad.
     * 
     * Estructura de la tabla intermedia producto_certificaciones:
     * - producto_id: Clave foránea que referencia a productos.producto_id
     * - certificacion_id: Clave foránea que referencia a Certificaciones.certificacion_id
     * 
     * @ManyToMany(mappedBy = "certificaciones"): Indica que la relación está mapeada
     * desde el lado de Producto, donde el campo se llama "certificaciones".
     * 
     * FetchType.LAZY: Carga los productos solo cuando se accede a ellos.
     * Esto evita problemas de rendimiento y referencias circulares durante la serialización.
     * 
     * @JsonIgnore: Evita que esta relación se serialice en JSON, previniendo
     * referencias circulares y problemas de rendimiento durante la serialización.
     * 
     * @EqualsAndHashCode.Exclude: Excluye este campo del equals y hashCode para evitar
     * referencias circulares y problemas de rendimiento.
     * 
     * @ToString.Exclude: Excluye este campo del toString para evitar referencias circulares.
     * 
     * @Builder.Default: Inicializa el Set como vacío por defecto cuando se usa Builder.
     */
    @ManyToMany(mappedBy = "certificaciones", fetch = FetchType.LAZY) // Relación bidireccional, carga lazy
    @JsonIgnore // Evita referencias circulares durante la serialización JSON
    @EqualsAndHashCode.Exclude // Excluye productos del equals y hashCode para evitar referencias circulares
    @ToString.Exclude // Excluye productos del toString para evitar referencias circulares
    @Builder.Default
    private Set<Producto> productos = new HashSet<>();
}

