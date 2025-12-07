package com.ecoshop.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

    /**
     * Entidad JPA que representa un usuario en la base de datos.
     * 
     * Esta clase mapea la tabla "Usuarios" en la base de datos PostgreSQL.
     * Cada instancia de esta clase representa una fila en la tabla.
     * 
     * Estructura de la tabla según el esquema:
     * - usuario_id: Identificador único (clave primaria, auto-generado)
     * - clerk_id: ID del usuario en Clerk (opcional, único, para integración con Clerk)
     * - email: Email del usuario (obligatorio, único)
     * - password_hash: Hash de la contraseña (opcional, nullable - Clerk maneja autenticación)
     * - nombre: Nombre del usuario (opcional, máximo 100 caracteres)
     * - direccion_default: Dirección por defecto del usuario (opcional, TEXT)
     * - rol: Rol del usuario (obligatorio, máximo 10 caracteres)
     *   Valores posibles: 'cliente', 'marca', 'admin'
     * - fecha_registro: Fecha de registro del usuario (obligatorio, no actualizable)
     * - eco_puntos: Eco-puntos acumulados del usuario (obligatorio, default 0)
     * - nivel_eco_puntos: Nivel actual de eco-puntos (opcional, máximo 20 caracteres)
     * - meta_eco_puntos: Meta de eco-puntos para el siguiente nivel (opcional)
     * - meta_co2: Meta de CO₂ ahorrado del usuario (opcional, decimal)
     * - meta_eco_puntos_personalizada: Meta personalizada de eco-puntos (opcional)
     * - co2_ahorrado_total: Total de CO₂ ahorrado acumulado (opcional, decimal, default 0)
     * - agua_ahorrada_total: Total de agua ahorrada acumulada (opcional, default 0)
     * - compras_sostenibles_total: Contador de compras sostenibles (opcional, default 0)
     * 
     * INTEGRACIÓN CON CLERK:
     * ======================
     * Esta entidad está integrada con Clerk (https://clerk.com),
     * servicio de autenticación y autorización.
     * 
     * Clerk ya está integrado y funcionando:
     * - La autenticación (login, registro, recuperación de contraseña) es manejada por Clerk
     * - El campo 'clerk_id' vincula el usuario local con el usuario en Clerk
     * - El campo 'password_hash' es opcional ya que Clerk maneja las contraseñas
     * - Los tokens JWT y sesiones son manejados por Clerk
     * - Los roles y permisos pueden sincronizarse desde Clerk usando webhooks
     * 
     * Flujo actual de integración:
     * 1. Usuario se registra en Clerk (frontend)
     * 2. Clerk envía webhook a este backend cuando se crea un usuario
     * 3. Backend crea/actualiza el Usuario local vinculándolo con clerk_id
     * 4. Para autenticación, el frontend obtiene token de Clerk y lo envía al backend
     * 5. Backend valida el token con Clerk usando ClerkJwtAuthenticationConverter
     * 
     * Relaciones:
     * - @OneToOne: Relación inversa con Marca (cada marca tiene un usuario)
     * - @OneToMany: Relación inversa con Pedido (cada pedido tiene un usuario)
     */
@Entity
@Table(name = "Usuarios")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class Usuario {

    /**
     * Identificador único del usuario.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /**
     * ID del usuario en Clerk.
     * 
     * Este campo vincula el usuario local con el usuario en Clerk.
     * Es opcional y único, permitiendo que algunos usuarios existan solo localmente
     * (útil durante la migración o para usuarios de prueba).
     * 
     * Con Clerk integrado:
     * - Este campo se llena automáticamente cuando Clerk envía un webhook de creación de usuario
     * - Se usa para buscar usuarios cuando se valida un token JWT de Clerk (ClerkJwtAuthenticationConverter)
     * - Permite sincronizar datos entre Clerk y la base de datos local mediante webhooks
     * 
     * @Column(unique = true): Garantiza que cada clerk_id sea único
     */
    @Column(name = "clerk_id", unique = true)
    private String clerkId;

    /**
     * Email del usuario.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(unique = true): Garantiza que cada email sea único
     * 
     * El email debe ser único en la base de datos y se usa como identificador
     * alternativo del usuario.
     * 
     * Con Clerk integrado:
     * - El email se sincroniza desde Clerk mediante webhooks
     * - Clerk garantiza que el email sea único y verificado
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hash de la contraseña del usuario.
     * 
     * @Column(nullable = true): Este campo es opcional cuando se usa Clerk
     * 
     * IMPORTANTE - INTEGRACIÓN CON CLERK:
     * ====================================
     * Con Clerk integrado, este campo es opcional (nullable) ya que
     * Clerk maneja todas las contraseñas y la autenticación.
     * 
     * Estado actual:
     * - Este campo puede ser null para usuarios autenticados con Clerk
     * - Solo se mantiene para usuarios legacy o casos especiales
     * - La autenticación se hace validando tokens JWT de Clerk mediante ClerkJwtAuthenticationConverter
     * - Los usuarios creados mediante webhooks de Clerk no requieren password_hash
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * Nombre del usuario.
     * 
     * @Column(length = 100): Longitud máxima de 100 caracteres en la BD
     * 
     * Campo opcional que almacena el nombre completo o nombre de display del usuario.
     * 
     * Con Clerk integrado:
     * - Este campo se sincroniza desde Clerk (firstName + lastName) mediante webhooks
     * - Clerk proporciona esta información en los tokens JWT y webhooks
     */
    @Column(length = 100)
    private String nombre;

    /**
     * Dirección por defecto del usuario.
     * 
     * @Column(columnDefinition = "TEXT"): Permite almacenar texto largo sin límite de caracteres
     * 
     * Campo opcional que almacena la dirección de envío por defecto del usuario.
     * Útil para prellenar formularios de pedidos.
     */
    @Column(name = "direccion_default", columnDefinition = "TEXT")
    private String direccionDefault;

    /**
     * Rol del usuario en el sistema.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(length = 10): Longitud máxima de 10 caracteres en la BD
     * 
     * Valores posibles:
     * - "cliente": Usuario regular que puede realizar pedidos
     * - "marca": Usuario que representa una marca y puede gestionar productos
     * - "admin": Administrador del sistema con acceso completo
     * 
     * Con Clerk integrado:
     * - Los roles pueden sincronizarse desde Clerk usando webhooks
     * - Clerk puede manejar roles y permisos más complejos
     * - Se puede usar metadata de Clerk para almacenar roles personalizados
     */
    @Column(nullable = false, length = 10)
    private String rol;

    /**
     * Fecha de registro del usuario.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha actual al crear la entidad
     * @Column(updatable = false): Este campo no se puede actualizar después de la creación
     * 
     * La fecha se establece automáticamente cuando se crea el usuario por primera vez
     * y no se puede modificar posteriormente.
     * 
     * Con Clerk integrado:
     * - Esta fecha puede sincronizarse con la fecha de creación en Clerk mediante webhooks
     * - Clerk proporciona createdAt en los webhooks
     */
    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Eco-puntos acumulados del usuario.
     * 
     * Representa la cantidad total de puntos ecológicos ganados por el usuario
     * a través de compras sostenibles. Los puntos se calculan automáticamente
     * cuando se crea o confirma un pedido.
     * 
     * @Column(nullable = false, columnDefinition = "integer default 0"): 
     * Este campo es obligatorio con valor por defecto de 0 en la base de datos
     * @Builder.Default: Valor por defecto de 0 para nuevos usuarios
     */
    @Column(name = "eco_puntos", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer ecoPuntos = 0;

    /**
     * Nivel actual de eco-puntos del usuario.
     * 
     * Niveles posibles:
     * - "Bronce": 0-100 puntos
     * - "Plata": 101-300 puntos
     * - "Oro": 301-500 puntos
     * - "Platino": 501+ puntos
     * 
     * Se calcula automáticamente basándose en la cantidad de eco-puntos acumulados.
     */
    @Column(name = "nivel_eco_puntos", length = 20)
    private String nivelEcoPuntos;

    /**
     * Meta de eco-puntos para alcanzar el siguiente nivel.
     * 
     * Representa la cantidad de puntos necesarios para subir al siguiente nivel.
     * Se calcula automáticamente basándose en el nivel actual.
     */
    @Column(name = "meta_eco_puntos")
    private Integer metaEcoPuntos;

    /**
     * Meta de CO₂ ahorrado del usuario (en kg).
     * 
     * Representa el objetivo de CO₂ ahorrado que el usuario se ha propuesto alcanzar.
     * El usuario puede configurar esta meta personalizada.
     * 
     * @Column(precision = 10, scale = 2): Permite valores decimales con 2 decimales
     */
    @Column(name = "meta_co2", precision = 10, scale = 2)
    private java.math.BigDecimal metaCO2;

    /**
     * Meta personalizada de eco-puntos del usuario.
     * 
     * Representa el objetivo de eco-puntos que el usuario se ha propuesto alcanzar.
     * Diferente de metaEcoPuntos que es para el siguiente nivel automático.
     * El usuario puede configurar esta meta personalizada.
     */
    @Column(name = "meta_eco_puntos_personalizada")
    private Integer metaEcoPuntosPersonalizada;

    /**
     * Total de CO₂ ahorrado acumulado por el usuario (en kg).
     * 
     * Representa la suma total de CO₂ ahorrado en todas las compras del usuario
     * comparado con productos convencionales equivalentes.
     * 
     * @Column(precision = 10, scale = 2): Permite valores decimales con 2 decimales
     */
    @Column(name = "co2_ahorrado_total", precision = 10, scale = 2)
    @Builder.Default
    private java.math.BigDecimal co2AhorradoTotal = java.math.BigDecimal.ZERO;

    /**
     * Total de agua ahorrada acumulada por el usuario (en litros).
     * 
     * Representa la suma total de agua ahorrada en todas las compras del usuario
     * comparado con productos convencionales equivalentes.
     */
    @Column(name = "agua_ahorrada_total")
    @Builder.Default
    private Integer aguaAhorradaTotal = 0;

    /**
     * Contador total de compras sostenibles realizadas por el usuario.
     * 
     * Representa el número total de pedidos que contienen productos sostenibles
     * (productos con bajo impacto, materiales reciclables, certificaciones, etc.).
     */
    @Column(name = "compras_sostenibles_total")
    @Builder.Default
    private Integer comprasSosteniblesTotal = 0;
}
