package com.ecoshop.service.producto;

import com.ecoshop.dto.Producto.ProductoPaginadoResponse;
import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio de productos.
 * 
 * Define el contrato para las operaciones de negocio relacionadas con productos.
 * Esta interfaz separa la definición de las operaciones de su implementación,
 * permitiendo cambiar la implementación sin afectar a los clientes (controladores).
 * 
 * Responsabilidades:
 * - Definir las operaciones CRUD (Create, Read, Update, Delete) para productos
 * - Trabajar con DTOs en lugar de entidades para mantener desacoplamiento
 * - Manejar la lógica de negocio relacionada con productos
 * 
 * ¿Por qué usar una interfaz de servicio?
 * - Separación de responsabilidades: define qué se hace, no cómo se hace
 * - Facilita el testing: permite crear implementaciones mock para pruebas
 * - Flexibilidad: permite cambiar la implementación sin afectar a los clientes
 * - Documentación: sirve como contrato claro de las operaciones disponibles
 * 
 * Operaciones disponibles:
 * - createProducto: Crea un nuevo producto
 * - getProductoById: Obtiene un producto por su ID
 * - getAllProductos: Obtiene todos los productos
 * - getProductosByMarca: Obtiene productos filtrados por marca
 * - updateProducto: Actualiza un producto existente
 * - deleteProducto: Elimina un producto
 */
public interface ProductoService {
    
    /**
     * Crea un nuevo producto en la base de datos.
     * 
     * Valida que la marca exista, que el SKU sea único (si se proporciona),
     * y que las certificaciones existan antes de crear el producto.
     * 
     * @param productoDTO Datos del producto a crear
     * @return ProductoResponseDTO con el producto creado y su ID asignado
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * @throws com.ecoshop.exception.BadRequestException si el SKU ya existe o si alguna certificación no existe
     */
    ProductoResponseDTO createProducto(ProductoRequestDTO productoDTO);
    
    /**
     * Obtiene un producto por su ID.
     * 
     * @param id Identificador único del producto
     * @return ProductoResponseDTO del producto encontrado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     */
    ProductoResponseDTO getProductoById(Integer id);
    
    /**
     * Obtiene todos los productos existentes.
     * 
     * @return Lista de todos los productos convertidos a ProductoResponseDTO
     */
    List<ProductoResponseDTO> getAllProductos();
    
    /**
     * Obtiene todos los productos con paginación.
     * 
     * @param page Número de página (basado en 1, por defecto 1)
     * @param size Tamaño de la página (por defecto 6)
     * @return ProductoPaginadoResponse con los productos paginados y metadatos de paginación
     */
    ProductoPaginadoResponse getAllProductosPaginados(Integer page, Integer size);
    
    /**
     * Obtiene todos los productos de una marca específica.
     * 
     * @param marcaId Identificador de la marca
     * @return Lista de productos de la marca convertidos a ProductoResponseDTO
     */
    List<ProductoResponseDTO> getProductosByMarca(Integer marcaId);

    /**
     * Obtiene todos los productos de una categoría específica.
     * 
     * @param categoriaId Identificador de la categoría
     * @return Lista de productos de la categoría convertidos a ProductoResponseDTO
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     */
    List<ProductoResponseDTO> getProductosByCategoria(Integer categoriaId);

    /**
     * Busca productos con múltiples filtros avanzados y paginación.
     * 
     * Permite filtrar productos por múltiples criterios simultáneamente con paginación:
     * - Categoría
     * - Impacto (eco badge)
     * - Marca
     * - Material
     * - Origen
     * - Rango de precio
     * - Certificación
     * 
     * También permite ordenar los resultados por diferentes criterios.
     * 
     * @param categoriaId ID de la categoría (opcional)
     * @param impacto Nivel de impacto: "bajo_impacto", "medio_impacto", "neutro" (opcional)
     * @param marcaId ID de la marca (opcional)
     * @param material Material a buscar en el campo materiales (opcional)
     * @param origen País/región de origen (opcional)
     * @param precioMin Precio mínimo (opcional)
     * @param precioMax Precio máximo (opcional)
     * @param certificacionId ID de la certificación (opcional)
     * @param ordenarPor Criterio de ordenamiento: "precio_asc", "precio_desc", "impacto_asc", "impacto_desc" (opcional)
     * @param page Número de página (basado en 1, por defecto 1)
     * @param size Tamaño de la página (por defecto 6)
     * @return ProductoPaginadoResponse con los productos filtrados, ordenados y paginados
     */
    ProductoPaginadoResponse buscarProductosPaginados(
            Integer categoriaId,
            String impacto,
            Integer marcaId,
            String material,
            String origen,
            java.math.BigDecimal precioMin,
            java.math.BigDecimal precioMax,
            Integer certificacionId,
            String ordenarPor,
            Integer page,
            Integer size);
    
    /**
     * Actualiza un producto existente.
     * 
     * Valida que el producto exista, que la marca exista (si se cambia),
     * que el SKU sea único (si se cambia), y que las certificaciones existan.
     * Preserva la fecha de creación original del producto.
     * 
     * @param id Identificador del producto a actualizar
     * @param productoDTO Nuevos datos del producto
     * @return ProductoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto o la marca no existen
     * @throws com.ecoshop.exception.BadRequestException si el SKU ya existe o si alguna certificación no existe
     */
    ProductoResponseDTO updateProducto(Integer id, ProductoRequestDTO productoDTO);
    
    /**
     * Elimina un producto de la base de datos.
     * 
     * Elimina primero las relaciones de certificaciones y luego el producto.
     * 
     * @param id Identificador del producto a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     */
    void deleteProducto(Integer id);

    /**
     * Obtiene las opciones disponibles para cada filtro.
     * 
     * Este método retorna todas las opciones disponibles para poblar
     * los filtros en el frontend (categorías, marcas, certificaciones, etc.).
     * 
     * @return FiltrosDisponiblesResponse con todas las opciones de filtros
     */
    com.ecoshop.dto.Producto.FiltrosDisponiblesResponse obtenerFiltrosDisponibles();
    
    /**
     * Busca productos por nombre con paginación (búsqueda parcial, case-insensitive).
     * 
     * @param query Término de búsqueda (nombre o parte del nombre)
     * @param page Número de página (basado en 1, por defecto 1)
     * @param size Tamaño de la página (por defecto 6)
     * @return ProductoPaginadoResponse con los productos que coinciden con el nombre, paginados
     */
    ProductoPaginadoResponse buscarProductosPorNombre(String query, Integer page, Integer size);
    
    /**
     * Sugiere productos más sostenibles como alternativas a un producto dado.
     * 
     * Las sugerencias se basan en:
     * - Misma categoría (si existe)
     * - Menor huella de carbono
     * - Mejor eco-badge
     * - Precio similar o menor
     * 
     * @param productoId ID del producto para el cual se buscan alternativas
     * @param limite Número máximo de sugerencias a retornar (por defecto 5)
     * @return SugerenciasProductosResponse con productos alternativos más sostenibles
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     */
    com.ecoshop.dto.Producto.SugerenciasProductosResponse sugerirProductosSostenibles(Integer productoId, Integer limite);
}
