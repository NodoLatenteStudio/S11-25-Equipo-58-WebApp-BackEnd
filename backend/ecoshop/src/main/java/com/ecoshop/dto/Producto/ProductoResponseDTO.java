package com.ecoshop.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) para respuestas de productos.
 * 
 * Esta clase representa los datos de un producto que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * ¿Por qué usar DTOs?
 * - Separación entre la capa de presentación y la capa de datos
 * - Control sobre qué datos se exponen en la API
 * - Flexibilidad para cambiar la estructura sin afectar la BD
 * - Mejor rendimiento al evitar cargar relaciones innecesarias
 * 
 * Diferencias con ProductoRequestDTO:
 * - Incluye campos calculados o derivados (productoId, nombreMarca)
 * - Incluye campos de solo lectura (fechaCreacion, activo)
 * - Las certificaciones se devuelven como nombres (no códigos) para el frontend
 * 
 * Campos:
 * - productoId: Identificador único del producto (generado por la BD)
 * - marcaId: ID de la marca a la que pertenece el producto
 * - nombreMarca: Nombre oficial de la marca (útil para el frontend)
 * - nombre: Nombre del producto
 * - descripcion: Descripción detallada del producto
 * - precio: Precio del producto
 * - stock: Cantidad disponible en inventario
 * - sku: Código SKU único del producto
 * - materiales: Materiales utilizados en la fabricación
 * - origen: Origen del producto
 * - huellaCarbonoTotal: Huella de carbono total en kg CO₂
 * - porcentajeReciclable: Porcentaje de material reciclable (0-100)
 * - ecoBadge: Badge ecológico del producto
 * - imagenUrl: URL de la imagen del producto
 * - activo: Indica si el producto está activo y disponible para venta
 * - fechaCreacion: Fecha de creación del producto en el sistema
 * - certificaciones: Lista de nombres de certificaciones (para el frontend)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    /**
     * Identificador único del producto.
     * 
     * Este campo se asigna automáticamente por la base de datos cuando se crea el producto.
     * Se incluye en todas las respuestas para que el cliente pueda identificar el producto.
     */
    private Integer productoId;

    /**
     * ID de la marca a la que pertenece el producto.
     * 
     * Útil para realizar operaciones relacionadas con la marca o para
     * filtrar productos por marca en el frontend.
     */
    private Integer marcaId;

    /**
     * Nombre oficial de la marca.
     * 
     * Este campo se incluye para facilitar la visualización en el frontend
     * sin necesidad de hacer una consulta adicional a la API de marcas.
     * 
     * Ejemplos: "EcoLife", "GreenTech", "SustainableBrand"
     */
    private String nombreMarca;

    /**
     * Nombre del producto.
     * 
     * Ejemplos: "Botella reutilizable EcoLife", "Bolsa de algodón orgánico"
     */
    private String nombre;

    /**
     * Descripción detallada del producto.
     * 
     * Proporciona información adicional sobre el producto.
     */
    private String descripcion;

    /**
     * Precio del producto.
     * 
     * Usamos BigDecimal para mantener precisión en cálculos monetarios.
     */
    private BigDecimal precio;

    /**
     * Cantidad disponible en inventario.
     * 
     * Representa la cantidad de unidades disponibles para la venta.
     */
    private Integer stock;

    /**
     * Código SKU único del producto.
     * 
     * Identificador único para gestión de inventario.
     */
    private String sku;

    /**
     * Materiales utilizados en la fabricación del producto.
     * 
     * Ejemplos: "Plástico reciclado", "Algodón orgánico", "Bambú"
     */
    private String materiales;

    /**
     * Origen del producto.
     * 
     * Indica el país o región de origen.
     * Ejemplos: "Argentina", "Chile", "Local"
     */
    private String origen;

    /**
     * Huella de carbono total del producto en kilogramos de CO₂.
     * 
     * Representa la cantidad total de emisiones de CO₂ asociadas al producto.
     */
    private BigDecimal huellaCarbonoTotal;

    /**
     * Porcentaje de material reciclable del producto.
     * 
     * Valor entre 0 y 100 que indica el porcentaje de material reciclable.
     */
    private Integer porcentajeReciclable;

    /**
     * Badge ecológico del producto.
     * 
     * Clasifica el impacto ambiental del producto.
     * Valores posibles: "bajo_impacto", "medio_impacto", "neutro"
     */
    private String ecoBadge;

    /**
     * URL de la imagen del producto.
     * 
     * Dirección de la imagen para mostrar en el frontend.
     */
    private String imagenUrl;

    /**
     * Indica si el producto está activo y disponible para venta.
     * 
     * true: El producto está activo y disponible
     * false: El producto está inactivo (no se muestra en el catálogo)
     */
    private Boolean activo;

    /**
     * Fecha de creación del producto en el sistema.
     * 
     * Se asigna automáticamente cuando se crea el producto.
     * No se actualiza cuando se modifica el producto.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Lista de nombres de certificaciones del producto.
     * 
     * IMPORTANTE: Este campo contiene los NOMBRES de las certificaciones (no códigos),
     * ya que se usa para mostrar información al usuario en el frontend.
     * 
     * Ejemplos: ["Fair Trade", "Carbon Neutral", "Organic"]
     * 
     * Nota: En ProductoRequestDTO se usan códigos (ej: ["FAIR_TRADE", "CARBON_NEUTRAL"]),
     * pero en la respuesta se devuelven nombres para facilitar la visualización.
     */
    private List<String> certificaciones;
}
