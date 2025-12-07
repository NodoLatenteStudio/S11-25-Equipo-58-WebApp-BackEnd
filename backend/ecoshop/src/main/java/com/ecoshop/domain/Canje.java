package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un canje de recompensa realizado por un usuario.
 * 
 * Esta clase mapea la tabla "Canjes" en la base de datos PostgreSQL.
 * Registra cada vez que un usuario canjea una recompensa usando sus eco-puntos.
 * 
 * Estructura de la tabla:
 * - canje_id: Identificador único (clave primaria, auto-generado)
 * - usuario_id: Clave foránea a la tabla Usuarios (obligatorio)
 * - recompensa_id: Clave foránea a la tabla Recompensas (obligatorio)
 * - puntos_usados: Cantidad de eco-puntos utilizados en el canje (obligatorio)
 * - estado: Estado del canje (obligatorio, máximo 20 caracteres)
 * - codigo_canje: Código único del canje para validación (opcional, máximo 100 caracteres)
 * - fecha_canje: Fecha en que se realizó el canje (obligatorio, no actualizable)
 * 
 * Relaciones:
 * - @ManyToOne: Relación con Usuario (usuario_id)
 * - @ManyToOne: Relación con Recompensa (recompensa_id)
 */
@Entity
@Table(name = "Canjes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Canje {

    /**
     * Identificador único del canje.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "canje_id")
    private Integer canjeId;

    /**
     * Relación Many-to-One con Usuario.
     * 
     * Cada canje pertenece a un usuario.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Usuario usuario;

    /**
     * Relación Many-to-One con Recompensa.
     * 
     * Cada canje está asociado a una recompensa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recompensa_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Recompensa recompensa;

    /**
     * Cantidad de eco-puntos utilizados en este canje.
     * 
     * Debe coincidir con los puntos requeridos de la recompensa al momento del canje.
     */
    @Column(name = "puntos_usados", nullable = false)
    private Integer puntosUsados;

    /**
     * Estado del canje.
     * 
     * Valores posibles:
     * - "pendiente": Canje realizado pero pendiente de procesamiento
     * - "completado": Canje procesado y entregado
     * - "cancelado": Canje cancelado
     * - "expirado": Canje expirado (si aplica)
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "pendiente";

    /**
     * Código único del canje para validación.
     * 
     * Útil para recompensas que requieren validación (descuentos, códigos promocionales, etc.)
     */
    @Column(name = "codigo_canje", length = 100, unique = true)
    private String codigoCanje;

    /**
     * Fecha en que se realizó el canje.
     * 
     * Se establece automáticamente cuando se crea el canje.
     */
    @CreationTimestamp
    @Column(name = "fecha_canje", updatable = false)
    private LocalDateTime fechaCanje;
}

