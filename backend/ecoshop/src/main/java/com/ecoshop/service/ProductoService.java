package com.ecoshop.service;

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
     * Obtiene todos los productos de una marca específica.
     * 
     * @param marcaId Identificador de la marca
     * @return Lista de productos de la marca convertidos a ProductoResponseDTO
     */
    List<ProductoResponseDTO> getProductosByMarca(Integer marcaId);
    
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
}
