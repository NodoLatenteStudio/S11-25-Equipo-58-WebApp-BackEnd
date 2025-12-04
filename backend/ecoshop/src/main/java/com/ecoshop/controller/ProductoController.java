package com.ecoshop.controller;

import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import com.ecoshop.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar productos.
 * 
 * Este controlador expone los endpoints CRUD para la gestión de productos.
 * Todos los endpoints están bajo la ruta base "/api/v1/productos".
 * 
 * Flujo de datos:
 * 1. El cliente envía una petición HTTP
 * 2. El controlador valida la petición (si aplica)
 * 3. El controlador delega la lógica de negocio al servicio
 * 4. El servicio procesa la petición y retorna un DTO
 * 5. El controlador envía la respuesta HTTP al cliente
 * 
 * Endpoints disponibles:
 * - POST /api/v1/productos - Crear un nuevo producto
 * - GET /api/v1/productos - Obtener todos los productos
 * - GET /api/v1/productos/{id} - Obtener un producto por ID
 * - GET /api/v1/productos/marca/{marcaId} - Obtener productos por marca
 * - PUT /api/v1/productos/{id} - Actualizar un producto
 * - DELETE /api/v1/productos/{id} - Eliminar un producto
 * 
 * Validaciones:
 * - Las validaciones de datos se realizan automáticamente mediante @Valid
 * - Si la validación falla, se lanza MethodArgumentNotValidException
 * - El GlobalExceptionHandler captura y formatea los errores de validación
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor // Genera automáticamente un constructor con los campos finales (inyección de dependencias)
public class ProductoController {

    // Inyección de dependencias: Spring proporciona automáticamente una instancia de ProductoService
    private final ProductoService productoService;

    /**
     * Crea un nuevo producto en la base de datos.
     * 
     * @param dto Datos del producto a crear (viene en el cuerpo de la petición)
     * @return ResponseEntity con el producto creado y código HTTP 201 (CREATED)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException si los datos no son válidos
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * @throws com.ecoshop.exception.BadRequestException si el SKU ya existe o si alguna certificación no existe
     * 
     * Validaciones aplicadas (definidas en ProductoRequestDTO):
     * - marcaId: obligatorio
     * - nombre: obligatorio, máximo 200 caracteres
     * - precio: obligatorio, mayor a 0
     * - stock: obligatorio, mínimo 0
     * - ecoBadge: solo acepta valores específicos (bajo_impacto, medio_impacto, neutro)
     * 
     * Ejemplo de uso:
     * POST http://localhost:8080/api/v1/productos
     * Body: {
     *   "marcaId": 1,
     *   "nombre": "Botella reutilizable",
     *   "precio": 14990,
     *   "stock": 50,
     *   "certificaciones": ["FAIR_TRADE", "CARBON_NEUTRAL"]
     * }
     */
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> createProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        // @Valid activa las validaciones definidas en ProductoRequestDTO
        // @RequestBody convierte el JSON del cuerpo de la petición a un objeto ProductoRequestDTO
        ProductoResponseDTO createdProducto = productoService.createProducto(dto);
        // Retornamos código HTTP 201 (CREATED) para indicar que se creó un nuevo recurso
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducto);
    }

    /**
     * Obtiene un producto específico por su ID.
     * 
     * @param id Identificador único del producto
     * @return ResponseEntity con el producto encontrado (ProductoResponseDTO) y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/productos/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> getProductoById(@PathVariable Integer id) {
        // @PathVariable extrae el valor del ID desde la URL
        ProductoResponseDTO producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * Obtiene todos los productos existentes en la base de datos.
     * 
     * @return ResponseEntity con la lista de productos (ProductoResponseDTO) y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> getAllProductos() {
        // Delegamos la lógica al servicio para mantener separación de responsabilidades
        List<ProductoResponseDTO> productos = productoService.getAllProductos();
        return ResponseEntity.ok(productos); // Retorna código HTTP 200 con la lista de productos
    }

    /**
     * Obtiene todos los productos de una marca específica.
     * 
     * Este endpoint es útil para filtrar productos por marca en el frontend.
     * 
     * @param marcaId Identificador de la marca
     * @return ResponseEntity con la lista de productos de la marca y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/productos/marca/1
     */
    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<List<ProductoResponseDTO>> getProductosByMarca(@PathVariable Integer marcaId) {
        // @PathVariable extrae el valor del marcaId desde la URL
        List<ProductoResponseDTO> productos = productoService.getProductosByMarca(marcaId);
        return ResponseEntity.ok(productos);
    }

    /**
     * Actualiza un producto existente en la base de datos.
     * 
     * @param id Identificador único del producto a actualizar
     * @param dto Nuevos datos del producto (viene en el cuerpo de la petición)
     * @return ResponseEntity con el producto actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto o la marca no existen
     * @throws org.springframework.web.bind.MethodArgumentNotValidException si los datos no son válidos
     * @throws com.ecoshop.exception.BadRequestException si el SKU ya existe o si alguna certificación no existe
     * 
     * Validaciones aplicadas (definidas en ProductoRequestDTO):
     * - Las mismas validaciones que en createProducto
     * 
     * Ejemplo de uso:
     * PUT http://localhost:8080/api/v1/productos/1
     * Body: {
     *   "nombre": "Botella actualizada",
     *   "precio": 15990,
     *   "stock": 30
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> updateProducto(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductoRequestDTO dto) {
        // @PathVariable extrae el ID desde la URL
        // @Valid activa las validaciones definidas en ProductoRequestDTO
        // @RequestBody convierte el JSON del cuerpo de la petición a un objeto ProductoRequestDTO
        ProductoResponseDTO updatedProducto = productoService.updateProducto(id, dto);
        return ResponseEntity.ok(updatedProducto);
    }

    /**
     * Elimina un producto de la base de datos.
     * 
     * @param id Identificador único del producto a eliminar
     * @return ResponseEntity vacío con código HTTP 204 (NO CONTENT)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de uso:
     * DELETE http://localhost:8080/api/v1/productos/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        // @PathVariable extrae el ID desde la URL
        productoService.deleteProducto(id);
        // Retornamos código HTTP 204 (NO CONTENT) para indicar que la operación fue exitosa
        // pero no hay contenido en la respuesta
        return ResponseEntity.noContent().build();
    }
}
