package com.ecoshop.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un contenido educativo en la base de datos.
 * 
 * Esta clase mapea la tabla "Contenido_Educativo" en la base de datos PostgreSQL.
 * Cada instancia de esta clase representa un artículo o historia educativa
 * sobre consumo responsable y sostenibilidad.
 * 
 * Estructura de la tabla:
 * - contenido_id: Identificador único (clave primaria, auto-generado)
 * - titulo: Título del contenido (obligatorio, máximo 200 caracteres)
 * - descripcion: Descripción breve del contenido (opcional, TEXT)
 * - contenido: Contenido completo del artículo (obligatorio, TEXT)
 * - categoria: Categoría del contenido (opcional, máximo 50 caracteres)
 * - imagen_url: URL de la imagen principal (opcional)
 * - autor: Autor del contenido (opcional, máximo 100 caracteres)
 * - fecha_creacion: Fecha de creación (obligatorio, no actualizable)
 * - fecha_actualizacion: Fecha de última actualización (obligatorio)
 * - activo: Indica si el contenido está activo y visible (obligatorio, por defecto true)
 */
@Entity
@Table(name = "Contenido_Educativo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoEducativo {

    /**
     * Identificador único del contenido educativo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contenido_id")
    private Integer contenidoId;

    /**
     * Título del contenido educativo.
     */
    @Column(nullable = false, length = 200)
    private String titulo;

    /**
     * Descripción breve del contenido.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Contenido completo del artículo.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    /**
     * Categoría del contenido educativo.
     * Ejemplos: "Consumo Responsable", "Sostenibilidad", "Reciclaje", "Huella de Carbono"
     */
    @Column(length = 50)
    private String categoria;

    /**
     * URL de la imagen principal del contenido.
     */
    @Column(name = "imagen_url")
    private String imagenUrl;

    /**
     * Autor del contenido educativo.
     */
    @Column(length = 100)
    private String autor;

    /**
     * Fecha de creación del contenido.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización del contenido.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Indica si el contenido está activo y visible.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}

