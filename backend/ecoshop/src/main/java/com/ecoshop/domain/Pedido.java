package com.ecoshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

    /**
     * Entidad JPA que representa un pedido en la base de datos.
     * 
     * Esta clase mapea la tabla "Pedidos" en la base de datos PostgreSQL.
     * Cada instancia de esta clase representa una fila en la tabla.
     * 
     * Estructura de la tabla según el esquema:
     * - pedido_id: Identificador único (clave primaria, auto-generado)
     * - usuario_id: Clave foránea a la tabla Usuarios (obligatorio)
     * - fecha_pedido: Fecha en que se realizó el pedido (obligatorio, no actualizable)
     * - estado: Estado del pedido (obligatorio, máximo 15 caracteres)
     *   Valores posibles: 'pendiente_pago', 'procesando', 'enviado', 'entregado', 'cancelado'
     * - total: Total del pedido en moneda (obligatorio, decimal)
     * - direccion_envio: Dirección de envío del pedido (obligatorio, TEXT)
     * - metodo_pago: Método de pago utilizado (opcional, máximo 50 caracteres)
     * - id_transaccion_pago: ID de la transacción de pago (opcional)
     * - huella_carbono_total_kg: Huella de carbono total del pedido en kg CO₂ (opcional, decimal)
     * - estado_pago: Estado del pago del pedido (opcional, máximo 20 caracteres, default "pendiente")
     *   Valores posibles: 'pendiente', 'procesando', 'completado', 'fallido', 'reembolsado'
     * - co2_ahorrado: CO₂ ahorrado en este pedido vs productos convencionales (opcional, decimal)
     * - agua_ahorrada: Agua ahorrada en este pedido vs productos convencionales (opcional)
     * - eco_puntos_ganados: Eco-puntos ganados por este pedido (opcional)
     * 
     * Relaciones:
     * - @ManyToOne: Relación con Usuario (usuario_id)
     * - Relación inversa con PedidoItem (cada pedido tiene múltiples items)
     * 
     * Notas importantes:
     * - La relación con Usuario es Many-to-One, lo que significa que cada pedido
     *   pertenece a un único usuario y cada usuario puede tener múltiples pedidos.
     * - La fecha de pedido se genera automáticamente al crear el pedido.
     * - El total del pedido se calcula generalmente a partir de los PedidoItems asociados.
     * - Se usa @JsonIgnore en la relación con Usuario para evitar referencias circulares
     *   durante la serialización JSON (el DTO manejará la exposición del usuarioId).
     */
@Entity
@Table(name = "Pedidos")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode (Lombok)
@Builder // Permite construir objetos usando el patrón Builder (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class Pedido {

    /**
     * Identificador único del pedido.
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * - La BD genera automáticamente el ID usando una secuencia o auto-incremento
     * - PostgreSQL usa SERIAL o BIGSERIAL para esto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pedido_id")
    private Integer pedidoId;

    /**
     * Relación Many-to-One con Usuario.
     * 
     * Cada pedido pertenece a un único usuario.
     * La relación se almacena mediante la columna usuario_id en la tabla Pedidos.
     * 
     * @ManyToOne: Relación muchos-a-uno con la entidad Usuario.
     * @JoinColumn: Especifica la columna de la clave foránea
     * - name: Nombre de la columna en la tabla Pedidos (usuario_id)
     * - nullable = false: La relación es obligatoria (no puede ser null)
     * 
     * @JsonIgnore: Evita referencias circulares durante la serialización JSON.
     * El DTO de respuesta (PedidoResponseDTO) manejará la exposición del usuarioId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore // Evita referencias circulares durante la serialización JSON
    @EqualsAndHashCode.Exclude // Excluye usuario del equals y hashCode para evitar referencias circulares
    @ToString.Exclude // Excluye usuario del toString para evitar referencias circulares
    private Usuario usuario;

    /**
     * Fecha en que se realizó el pedido.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha actual al crear la entidad
     * @Column(updatable = false): Este campo no se puede actualizar después de la creación
     * 
     * La fecha se establece automáticamente cuando se crea el pedido por primera vez
     * y no se puede modificar posteriormente.
     */
    @CreationTimestamp
    @Column(name = "fecha_pedido", updatable = false)
    private LocalDateTime fechaPedido;

    /**
     * Estado del pedido.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(length = 15): Longitud máxima de 15 caracteres en la BD
     * 
     * Valores posibles:
     * - "pendiente_pago": El pedido está pendiente de pago
     * - "procesando": El pedido está siendo procesado
     * - "enviado": El pedido ha sido enviado
     * - "entregado": El pedido ha sido entregado
     * - "cancelado": El pedido ha sido cancelado
     * 
     * Por defecto, cuando se crea un pedido, el estado es "pendiente_pago".
     */
    @Column(nullable = false, length = 15)
    private String estado;

    /**
     * Total del pedido en moneda.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * 
     * Usamos BigDecimal en lugar de double para evitar problemas de precisión
     * con cálculos monetarios. El total generalmente se calcula a partir de
     * los PedidoItems asociados (suma de cantidad * precio_unitario).
     * 
     * Ejemplos: 14990.00, 15990.50, 9999.99
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Dirección de envío del pedido.
     * 
     * @Column(nullable = false): Este campo es obligatorio (no puede ser null)
     * @Column(columnDefinition = "TEXT"): Permite almacenar texto largo sin límite de caracteres
     * 
     * Almacena la dirección completa donde se debe enviar el pedido.
     * Ejemplos: "Av. Principal 123, Santiago, Chile", "Calle 45 #67-89, Bogotá, Colombia"
     */
    @Column(name = "direccion_envio", nullable = false, columnDefinition = "TEXT")
    private String direccionEnvio;

    /**
     * Método de pago utilizado.
     * 
     * Campo opcional que indica el método de pago utilizado para el pedido.
     * Ejemplos: "tarjeta_credito", "transferencia_bancaria", "paypal", "efectivo"
     */
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    /**
     * ID de la transacción de pago.
     * 
     * Campo opcional que almacena el identificador de la transacción de pago
     * proporcionado por el procesador de pagos (ej: Stripe, PayPal, etc.).
     * Útil para rastrear y verificar pagos.
     */
    @Column(name = "id_transaccion_pago")
    private String idTransaccionPago;

    /**
     * Huella de carbono total del pedido en kilogramos de CO₂.
     * 
     * Campo opcional que representa la cantidad total de emisiones de CO₂
     * asociadas al pedido. Se calcula generalmente sumando la huella de carbono
     * de todos los productos incluidos en el pedido.
     * 
     * Ejemplos: 2.5, 5.8, 10.2
     */
    @Column(name = "huella_carbono_total_kg", precision = 10, scale = 2)
    private BigDecimal huellaCarbonoTotalKg;

    /**
     * Estado del pago del pedido.
     * 
     * Indica el estado actual del proceso de pago.
     * Valores posibles:
     * - "pendiente": El pago está pendiente
     * - "procesando": El pago está siendo procesado
     * - "completado": El pago fue completado exitosamente
     * - "fallido": El pago falló
     * - "reembolsado": El pago fue reembolsado
     * 
     * @Column(length = 20): Longitud máxima de 20 caracteres
     */
    @Column(name = "estado_pago", length = 20)
    @Builder.Default
    private String estadoPago = "pendiente";

    /**
     * CO₂ ahorrado en este pedido comparado con productos convencionales (en kg).
     * 
     * Representa la diferencia de emisiones entre los productos sostenibles
     * de este pedido y productos convencionales equivalentes.
     * 
     * @Column(precision = 10, scale = 2): Permite valores decimales con 2 decimales
     */
    @Column(name = "co2_ahorrado", precision = 10, scale = 2)
    private BigDecimal co2Ahorrado;

    /**
     * Agua ahorrada en este pedido comparado con productos convencionales (en litros).
     * 
     * Representa la diferencia de consumo de agua entre los productos sostenibles
     * de este pedido y productos convencionales equivalentes.
     */
    @Column(name = "agua_ahorrada")
    private Integer aguaAhorrada;

    /**
     * Eco-puntos ganados por este pedido.
     * 
     * Representa la cantidad de eco-puntos que el usuario ganó al realizar este pedido.
     * Se calcula automáticamente cuando el pedido se confirma o entrega.
     */
    @Column(name = "eco_puntos_ganados")
    private Integer ecoPuntosGanados;
}
