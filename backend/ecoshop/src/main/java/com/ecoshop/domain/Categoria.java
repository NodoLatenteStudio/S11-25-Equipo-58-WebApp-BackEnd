package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad JPA que representa una categoría de productos en la base de datos.
 * 
 * Esta clase mapea la tabla "Categorias" en la base de datos PostgreSQL.
 * Cada categoría agrupa productos relacionados (ej: "Ropa", "Hogar", "Accesorios").
 * 
 * Estructura de la tabla:
 * - categoria_id: Identificador único (clave primaria, auto-generado)
 * - nombre: Nombre de la categoría (obligatorio, único)
 * - descripcion: Descripción de la categoría (opcional)
 * - fecha_creacion: Fecha en que se creó la categoría (auto-generada)
 * 
 * Relaciones:
 * - @OneToMany: Relación inversa con Producto (cada categoría puede tener múltiples productos)
 * 
 * Notas importantes:
 * - El nombre de la categoría debe ser único
 * - Se usa @JsonIgnore en la relación con Producto para evitar referencias circulares
 */
@Entity
@Table(name = "Categorias", uniqueConstraints = {
    @UniqueConstraint(columnNames = "nombre")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    /**
     * Identificador único de la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoria_id")
    private Integer categoriaId;

    /**
     * Nombre de la categoría.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(unique = true): El nombre debe ser único
     * @Column(length = 100): Longitud máxima de 100 caracteres
     * 
     * Ejemplos: "Ropa", "Hogar", "Accesorios", "Cuidado Personal", "Tecnología"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Descripción de la categoría.
     * 
     * @Column(columnDefinition = "TEXT"): Permite almacenar texto largo sin límite de caracteres
     * 
     * Campo opcional que describe qué tipo de productos pertenecen a esta categoría.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Fecha en que se creó la categoría.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha actual al crear la entidad
     * @Column(updatable = false): Este campo no se puede actualizar después de la creación
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Relación One-to-Many con Producto.
     * 
     * Cada categoría puede tener múltiples productos.
     * La relación se almacena mediante la columna categoria_id en la tabla Productos.
     * 
     * @OneToMany: Relación uno-a-muchos con la entidad Producto.
     * @mappedBy: Indica que la relación está mapeada por el campo "categoria" en Producto
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta manejará la exposición de los productos si es necesario.
     */
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Producto> productos;
}

