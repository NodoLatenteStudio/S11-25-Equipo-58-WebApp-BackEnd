package com.ecoshop.dto.Producto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object (DTO) para crear y actualizar productos.
 * 
 * Esta clase representa los datos de un producto que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) y actualización (PUT).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se aceptan en la API
 * - Validación automática de datos antes de procesarlos
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotNull, @NotBlank, @Size, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * Si la validación falla, se lanza MethodArgumentNotValidException.
 * 
 * Campos:
 * - marcaId: ID de la marca a la que pertenece el producto (obligatorio)
 * - nombre: Nombre del producto (obligatorio, máximo 200 caracteres)
 * - descripcion: Descripción detallada del producto (opcional)
 * - precio: Precio del producto (obligatorio, mayor a 0)
 * - stock: Cantidad disponible en inventario (obligatorio, mínimo 0)
 * - sku: Código SKU único del producto (opcional, único)
 * - materiales: Materiales utilizados en la fabricación (opcional)
 * - origen: Origen del producto (opcional)
 * - huellaCarbonoTotal: Huella de carbono total en kg CO₂ (opcional)
 * - porcentajeReciclable: Porcentaje de material reciclable (opcional, 0-100)
 * - ecoBadge: Badge ecológico del producto (opcional, valores: bajo_impacto, medio_impacto, neutro)
 * - imagenUrl: URL de la imagen del producto (opcional)
 * - certificaciones: Lista de códigos de certificaciones (opcional, ej: ["FAIR_TRADE", "CARBON_NEUTRAL"])
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    /**
     * ID de la marca a la que pertenece el producto.
     * 
     * @NotNull: Este campo es obligatorio (no puede ser null)
     * 
     * La marca debe existir en la base de datos. Si no existe, se lanzará
     * una excepción ResourceNotFoundException.
     */
    @NotNull(message = "El ID de la marca es obligatorio")
    private Integer marcaId;

    /**
     * Nombre del producto.
     * 
     * @NotBlank: El campo no puede estar vacío ni ser null
     * @Size: Longitud máxima de 200 caracteres
     * 
     * Ejemplos: "Botella reutilizable EcoLife", "Bolsa de algodón orgánico"
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    /**
     * Descripción detallada del producto.
     * 
     * Campo opcional que permite proporcionar información adicional sobre el producto.
     * Ejemplos: "Botella reutilizable de acero inoxidable con capacidad de 500ml"
     */
    private String descripcion;

    /**
     * Precio del producto.
     * 
     * @NotNull: El campo no puede ser null (obligatorio)
     * @DecimalMin: El valor debe ser mayor a 0 (no puede ser 0 ni negativo)
     * 
     * Usamos BigDecimal para evitar problemas de precisión con cálculos monetarios.
     * Ejemplos válidos: 14990.00, 15990.50, 9999.99
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    /**
     * Cantidad disponible en inventario.
     * 
     * @NotNull: El campo no puede ser null (obligatorio)
     * @Min: El valor no puede ser negativo (mínimo 0)
     * 
     * Representa la cantidad de unidades disponibles para la venta.
     */
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    /**
     * Código SKU (Stock Keeping Unit) único del producto.
     * 
     * Campo opcional que identifica de forma única el producto para gestión de inventario.
     * Si se proporciona, debe ser único en la base de datos.
     * 
     * Ejemplos: "BOT-ECO-500", "BOL-ALG-001"
     */
    private String sku;

    /**
     * Materiales utilizados en la fabricación del producto.
     * 
     * Campo opcional que describe los materiales del producto.
     * Ejemplos: "Plástico reciclado", "Algodón orgánico", "Bambú"
     */
    private String materiales;

    /**
     * Origen del producto.
     * 
     * Campo opcional que indica el país o región de origen.
     * Ejemplos: "Argentina", "Chile", "Local"
     */
    private String origen;

    /**
     * Huella de carbono total del producto en kilogramos de CO₂.
     * 
     * Campo opcional que representa la cantidad total de emisiones de CO₂
     * asociadas al producto durante su ciclo de vida.
     * 
     * Ejemplos: 0.8, 1.2, 2.5
     */
    private BigDecimal huellaCarbonoTotal;

    /**
     * Porcentaje de material reciclable del producto.
     * 
     * Campo opcional que indica el porcentaje de material reciclable (0-100).
     * Ejemplos: 0, 50, 100
     */
    private Integer porcentajeReciclable;

    /**
     * Badge ecológico del producto.
     * 
     * @Pattern: Solo acepta valores específicos: "bajo_impacto", "medio_impacto", "neutro"
     * 
     * Campo opcional que clasifica el impacto ambiental del producto.
     */
    @Pattern(regexp = "bajo_impacto|medio_impacto|neutro", message = "Valores permitidos: bajo_impacto, medio_impacto, neutro")
    private String ecoBadge;

    /**
     * URL de la imagen del producto.
     * 
     * Campo opcional que almacena la dirección de la imagen del producto.
     * Útil para mostrar el producto en el frontend.
     */
    private String imagenUrl;

    /**
     * Lista de IDs de certificaciones del producto.
     * 
     * Campo opcional que contiene una lista de IDs de certificaciones.
     * Estos IDs representan los identificadores únicos de las certificaciones
     * (ej: ["1", "2", "3"]).
     * 
     * IMPORTANTE: Este campo contiene los IDs de las certificaciones (como String).
     * Los IDs deben existir en la base de datos. Si algún ID no existe,
     * se lanzará una excepción BadRequestException (400 Bad Request).
     * 
     * No tiene validaciones de formato, puede ser null o una lista vacía.
     * La validación de existencia se realiza en el servicio.
     */
    private List<String> certificaciones;
}
