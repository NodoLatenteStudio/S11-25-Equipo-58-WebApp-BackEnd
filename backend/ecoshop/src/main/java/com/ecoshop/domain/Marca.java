package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una marca en la base de datos.
 * 
 * Esta clase mapea la tabla "Marcas" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa una fila en la tabla.
 * 
 * Estructura de la tabla según el esquema:
 * - marca_id: Identificador único (clave primaria, auto-generado)
 * - usuario_id: Clave foránea a la tabla Usuarios (obligatorio)
 * - nombre_oficial: Nombre oficial de la marca (obligatorio, máximo 150 caracteres)
 * - descripcion_sostenible: Descripción de las prácticas sostenibles de la marca (opcional, TEXT)
 * - sitio_web: URL del sitio web de la marca (opcional)
 * - logo_url: URL del logo de la marca (opcional)
 * - fecha_union: Fecha en que la marca se unió a la plataforma (obligatorio, no actualizable)
 * 
 * Relaciones:
 * - @OneToOne: Relación con Usuario (usuario_id)
 * - @OneToMany: Relación inversa con Producto (cada producto tiene una marca)
 * 
 * Notas importantes:
 * - La relación con Usuario es One-to-One, lo que significa que cada marca
 *   pertenece a un único usuario y cada usuario puede tener una única marca.
 * - La fecha de unión se genera automáticamente al crear la marca.
 * - Se usa @JsonIgnore en la relación con Usuario para evitar referencias circulares
 *   durante la serialización JSON (el DTO manejará la exposición del usuarioId).
 */
@Entity
@Table(name = "Marcas")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class Marca {

    /**
     * Identificador único de la marca.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marca_id")
    private Integer marcaId;

    /**
     * Relación One-to-One con Usuario.
     * 
     * Cada marca pertenece a un único usuario.
     * La relación se almacena mediante la columna usuario_id en la tabla Marcas.
     * 
     * @OneToOne: Relación uno-a-uno con la entidad Usuario.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Marcas (usuario_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta (MarcaResponseDTO) manejará la exposición del usuarioId.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore // Evita referencias circulares durante la serialización JSON
    @EqualsAndHashCode.Exclude // Excluye usuario del equals y hashCode para evitar referencias circulares
    @ToString.Exclude // Excluye usuario del toString para evitar referencias circulares
    private Usuario usuario;

    /**
     * Nombre oficial de la marca.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(length = 150): Longitud máxima de 150 caracteres en la BD
     * 
     * Ejemplos: "EcoLife", "GreenTech Solutions", "Sustentable S.A."
     */
    @Column(name = "nombre_oficial", nullable = false, length = 150)
    private String nombreOficial;

    /**
     * Descripción de las prácticas sostenibles de la marca.
     * 
     * Campo opcional que permite almacenar descripciones largas sobre
     * las prácticas sostenibles, certificaciones y compromisos ambientales de la marca.
     * 
     * @Column(columnDefinition = "TEXT"): Permite almacenar texto largo sin límite de caracteres
     */
    @Column(name = "descripcion_sostenible", columnDefinition = "TEXT")
    private String descripcionSostenible;

    /**
     * URL del sitio web de la marca.
     * 
     * Campo opcional que almacena la dirección del sitio web oficial de la marca.
     * Ejemplos: "https://www.ecolife.com", "https://greentech.com"
     */
    @Column(name = "sitio_web")
    private String sitioWeb;

    /**
     * URL del logo de la marca.
     * 
     * Campo opcional que almacena la dirección de la imagen del logo de la marca.
     * Útil para mostrar el logo en el frontend.
     * Ejemplos: "https://cdn.example.com/logos/ecolife.png"
     */
    @Column(name = "logo_url")
    private String logoUrl;

    /**
     * Fecha en que la marca se unió a la plataforma.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha actual al crear la entidad
     * @Column(updatable = false): Este campo no se puede actualizar después de la creación
     * 
     * La fecha se establece automáticamente cuando se crea la marca por primera vez
     * y no se puede modificar posteriormente.
     */
    @CreationTimestamp
    @Column(name = "fecha_union", updatable = false)
    private LocalDateTime fechaUnion;
}
