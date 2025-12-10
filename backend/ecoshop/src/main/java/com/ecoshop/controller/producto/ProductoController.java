package com.ecoshop.controller.producto;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.ImpactoAmbiental.ComparacionProductosResponse;
import com.ecoshop.dto.ImpactoAmbiental.MetricasAmbientalesResponse;
import com.ecoshop.dto.Marca.MarcaResponseDTO;
import com.ecoshop.dto.Producto.FiltrosDisponiblesResponse;
import com.ecoshop.dto.Producto.ProductoPaginadoResponse;
import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import com.ecoshop.dto.Producto.SugerenciasProductosResponse;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.marca.MarcaService;
import com.ecoshop.service.producto.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles:
 * ✅ POST /api/v1/productos - Crear un nuevo producto (público)
 * ✅ GET /api/v1/productos - Obtener todos los productos con filtros y paginación (público)
 * ✅ GET /api/v1/productos/{id} - Obtener un producto por ID (público)
 * ✅ GET /api/v1/productos/marca/{marcaId} - Obtener productos por marca (público)
 * ✅ PUT /api/v1/productos/{id} - Actualizar un producto (requiere autenticación + permisos)
 * ✅ DELETE /api/v1/productos/{id} - Eliminar un producto (requiere autenticación + permisos)
 * ✅ GET /api/v1/productos/{id}/metricas-ambientales - Obtener métricas ambientales (público)
 * ✅ GET /api/v1/productos/filtros/disponibles - Obtener opciones de filtros (público)
 * ✅ GET /api/v1/productos/comparar?ids=... - Comparar múltiples productos (público)
 * ✅ GET /api/v1/productos/buscar?q=... - Buscar productos por nombre (público)
 * ✅ GET /api/v1/productos/{id}/sugerencias - Sugerencias de productos sostenibles (público)
 * ✅ GET /api/v1/productos/{id}/trazabilidad - Trazabilidad completa del producto (público)
 * ✅ GET /api/v1/productos/{id}/viaje - Viaje del producto (alias de trazabilidad) (público)
 * 
 * Validaciones:
 * - Las validaciones de datos se realizan automáticamente mediante @Valid
 * - Si la validación falla, se lanza MethodArgumentNotValidException
 * - El GlobalExceptionHandler captura y formatea los errores de validación
 * - PUT y DELETE validan permisos: solo el dueño de la marca o admin pueden modificar/eliminar
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor // Genera automáticamente un constructor con los campos finales (inyección de dependencias)
public class ProductoController {

    // Inyección de dependencias: Spring proporciona automáticamente una instancia de ProductoService
    private final ProductoService productoService;
    
    // Servicio para calcular impacto ambiental
    private final ImpactoAmbientalService impactoAmbientalService;
    
    // Servicio para gestionar marcas (necesario para validar permisos)
    private final MarcaService marcaService;

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
     * Obtiene productos con filtros avanzados opcionales y paginación.
     * 
     * Este endpoint soporta múltiples filtros, ordenamiento y paginación:
     * - Filtros: categoria, impacto, marca, material, origen, precioMin, precioMax, certificacion
     * - Ordenamiento: precio_asc, precio_desc, impacto_asc, impacto_desc
     * - Paginación: page (número de página, basado en 1), size (tamaño de página, por defecto 6)
     * 
     * TODOS los resultados están paginados, incluso cuando se aplican filtros.
     * Por defecto, se muestran 6 productos por página según requerimiento UX/UI.
     * La primera página es la número 1 (no 0).
     * 
     * @param categoria ID de la categoría (opcional)
     * @param impacto Nivel de impacto: "bajo_impacto", "medio_impacto", "neutro" (opcional)
     * @param marca ID de la marca (opcional)
     * @param material Material a buscar (opcional)
     * @param origen País/región de origen (opcional)
     * @param precioMin Precio mínimo (opcional)
     * @param precioMax Precio máximo (opcional)
     * @param certificacion ID de la certificación (opcional)
     * @param ordenarPor Criterio de ordenamiento (opcional)
     * @param page Número de página (opcional, basado en 1, por defecto 1)
     * @param size Tamaño de página (opcional, por defecto 6 según requerimiento UX/UI)
     * @return ResponseEntity con productos paginados y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos
     * GET /api/v1/productos?page=1&size=6
     * GET /api/v1/productos?categoria=1&impacto=bajo_impacto&precioMin=1000&precioMax=50000&ordenarPor=precio_asc&page=1&size=6
     * GET /api/v1/productos?page=2&size=6&categoria=1
     */
    @GetMapping
    public ResponseEntity<ProductoPaginadoResponse> getAllProductos(
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String impacto,
            @RequestParam(required = false) Integer marca,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) java.math.BigDecimal precioMin,
            @RequestParam(required = false) java.math.BigDecimal precioMax,
            @RequestParam(required = false) Integer certificacion,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Verificar si hay filtros
        boolean tieneFiltros = categoria != null || impacto != null || marca != null || 
                              material != null || origen != null || precioMin != null || 
                              precioMax != null || certificacion != null || ordenarPor != null;
        
        // Si hay filtros, usar búsqueda paginada con filtros
        if (tieneFiltros) {
            ProductoPaginadoResponse productosPaginados = productoService.buscarProductosPaginados(
                    categoria, impacto, marca, material, origen, 
                    precioMin, precioMax, certificacion, ordenarPor, page, size);
            return ResponseEntity.ok(productosPaginados);
        }
        
        // Si no hay filtros, usar paginación simple
        ProductoPaginadoResponse productosPaginados = productoService.getAllProductosPaginados(page, size);
        return ResponseEntity.ok(productosPaginados);
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
     * Este endpoint permite actualizaciones parciales: solo se actualizan los campos
     * que se envían en el body. Los campos que no se envían permanecen sin cambios.
     * 
     * @param id Identificador único del producto a actualizar
     * @param dto Datos del producto a actualizar (viene en el cuerpo de la petición)
     * @return ResponseEntity con el producto actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto o la marca no existen
     * @throws com.ecoshop.exception.BadRequestException si el SKU ya existe o si alguna certificación no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos para actualizar el producto
     * 
     * Ejemplo de uso - Actualización parcial (solo imagenUrl):
     * PUT http://localhost:8080/api/v1/productos/1
     * Body: {
     *   "imagenUrl": "https://i.ibb.co/xxxxx/producto.jpg"
     * }
     * 
     * Ejemplo de uso - Actualización completa:
     * PUT http://localhost:8080/api/v1/productos/1
     * Body: {
     *   "nombre": "Botella actualizada",
     *   "precio": 15990,
     *   "stock": 30,
     *   "imagenUrl": "https://i.ibb.co/xxxxx/producto.jpg"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> updateProducto(
            @PathVariable Integer id, 
            @RequestBody ProductoRequestDTO dto,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede actualizar productos de su propia marca o ser admin
        ProductoResponseDTO productoExistente = productoService.getProductoById(id);
        MarcaResponseDTO marcaDelProducto = marcaService.getMarcaById(productoExistente.getMarcaId());
        
        // Validación de permisos: usuario debe ser dueño de la marca o admin
        // Si el producto se está moviendo a otra marca, validar que el usuario sea dueño de la nueva marca
        boolean puedeActualizarMarcaActual = marcaDelProducto.getUsuarioId().equals(usuario.getUsuarioId());
        boolean esAdmin = "admin".equalsIgnoreCase(usuario.getRol());
        
        // Si está cambiando la marca (y se proporciona un nuevo marcaId), validar que sea dueño de la nueva marca
        if (dto.getMarcaId() != null && !productoExistente.getMarcaId().equals(dto.getMarcaId())) {
            MarcaResponseDTO nuevaMarca = marcaService.getMarcaById(dto.getMarcaId());
            boolean puedeActualizarNuevaMarca = nuevaMarca.getUsuarioId().equals(usuario.getUsuarioId());
            
            if (!puedeActualizarMarcaActual && !puedeActualizarNuevaMarca && !esAdmin) {
                throw new ForbiddenException(
                    String.format("No tienes permisos para actualizar este producto. " +
                        "El producto pertenece a la marca %d (usuario %d) y estás intentando cambiarlo a la marca %d. " +
                        "Solo puedes actualizar productos de tu propia marca o ser admin.",
                        productoExistente.getMarcaId(), marcaDelProducto.getUsuarioId(), dto.getMarcaId())
                );
            }
        } else {
            // Si no cambia la marca (o no se proporciona marcaId en actualización parcial), validar que sea dueño de la marca actual
            if (!puedeActualizarMarcaActual && !esAdmin) {
                throw new ForbiddenException(
                    String.format("No tienes permisos para actualizar este producto. " +
                        "El producto pertenece a la marca %d (usuario %d) y tu usuarioId es %d. " +
                        "Solo puedes actualizar productos de tu propia marca o ser admin.",
                        productoExistente.getMarcaId(), marcaDelProducto.getUsuarioId(), usuario.getUsuarioId())
                );
            }
        }
        
        ProductoResponseDTO updatedProducto = productoService.updateProducto(id, dto);
        return ResponseEntity.ok(updatedProducto);
    }

    /**
     * Obtiene las métricas ambientales detalladas de un producto.
     * 
     * Este endpoint proporciona un desglose completo de todas las métricas ambientales
     * asociadas al producto, incluyendo emisiones por categoría, consumo de agua,
     * y comparaciones con productos convencionales.
     * 
     * @param id Identificador único del producto
     * @return ResponseEntity con las métricas ambientales detalladas y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/productos/1/metricas-ambientales
     */
    @GetMapping("/{id}/metricas-ambientales")
    public ResponseEntity<MetricasAmbientalesResponse> getMetricasAmbientales(@PathVariable Integer id) {
        MetricasAmbientalesResponse metricas = impactoAmbientalService.obtenerMetricasAmbientales(id);
        return ResponseEntity.ok(metricas);
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
    public ResponseEntity<Void> deleteProducto(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede eliminar productos de su propia marca o ser admin
        ProductoResponseDTO productoExistente = productoService.getProductoById(id);
        MarcaResponseDTO marcaDelProducto = marcaService.getMarcaById(productoExistente.getMarcaId());
        
        if (!marcaDelProducto.getUsuarioId().equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para eliminar este producto");
        }
        
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene las opciones disponibles para cada filtro.
     * 
     * Este endpoint proporciona todas las opciones disponibles para poblar
     * los filtros en el frontend (categorías, marcas, certificaciones, materiales, etc.).
     * 
     * @return ResponseEntity con las opciones de filtros disponibles y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos/filtros/disponibles
     */
    @GetMapping("/filtros/disponibles")
    public ResponseEntity<FiltrosDisponiblesResponse> getFiltrosDisponibles() {
        FiltrosDisponiblesResponse filtros = productoService.obtenerFiltrosDisponibles();
        return ResponseEntity.ok(filtros);
    }

    /**
     * Compara múltiples productos lado a lado.
     * 
     * Este endpoint permite comparar hasta 4 productos mostrando:
     * - Huella de carbono de cada producto
     * - Precio
     * - Materiales y origen
     * - Eco badge (nivel de impacto)
     * - Porcentaje reciclable
     * - Producto recomendado (balance entre precio e impacto)
     * - Producto con menor impacto
     * - Producto con menor precio
     * 
     * @param ids Lista de IDs de productos separados por comas (máximo 4)
     * @return ResponseEntity con la comparación detallada y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos/comparar?ids=1,2,3
     */
    @GetMapping("/comparar")
    public ResponseEntity<ComparacionProductosResponse> compararProductos(
            @RequestParam("ids") String ids) {
        // Convertir string de IDs separados por comas a lista de enteros
        List<Integer> productoIds = List.of(ids.split(","))
                .stream()
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        
        ComparacionProductosResponse comparacion = impactoAmbientalService.compararProductos(productoIds);
        return ResponseEntity.ok(comparacion);
    }

    /**
     * Busca productos por nombre con paginación.
     * 
     * Endpoint: GET /api/v1/productos/buscar?q={query}&page={page}&size={size}
     * 
     * Este endpoint permite buscar productos por nombre (búsqueda parcial, case-insensitive).
     * Solo busca en el campo nombre, no en la descripción.
     * Los resultados están paginados (6 productos por página por defecto, primera página es 1).
     * 
     * @param query Término de búsqueda (nombre o parte del nombre)
     * @param page Número de página (opcional, basado en 1, por defecto 1)
     * @param size Tamaño de página (opcional, por defecto 6 según requerimiento UX/UI)
     * @return ResponseEntity con productos paginados y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos/buscar?q=botella
     * GET /api/v1/productos/buscar?q=reutilizable&page=1&size=6
     * GET /api/v1/productos/buscar?q=botella&page=2&size=6
     */
    @GetMapping("/buscar")
    public ResponseEntity<ProductoPaginadoResponse> buscarProductosPorNombre(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        ProductoPaginadoResponse productos = productoService.buscarProductosPorNombre(query, page, size);
        return ResponseEntity.ok(productos);
    }

    /**
     * Sugiere productos más sostenibles como alternativas a un producto dado.
     * 
     * Endpoint: GET /api/v1/productos/{id}/sugerencias?limite={limite}
     * 
     * Este endpoint proporciona sugerencias de productos alternativos más sostenibles
     * basándose en:
     * - Misma categoría (si existe)
     * - Menor huella de carbono
     * - Mejor eco-badge
     * - Precio similar o menor
     * 
     * Las sugerencias están ordenadas por sostenibilidad (mejor primero).
     * 
     * @param id Identificador del producto para el cual se buscan alternativas
     * @param limite Número máximo de sugerencias a retornar (opcional, por defecto 5)
     * @return ResponseEntity con productos alternativos más sostenibles y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/productos/1/sugerencias
     * GET /api/v1/productos/1/sugerencias?limite=10
     */
    @GetMapping("/{id}/sugerencias")
    public ResponseEntity<SugerenciasProductosResponse> sugerirProductosSostenibles(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer limite) {
        SugerenciasProductosResponse sugerencias = productoService.sugerirProductosSostenibles(id, limite);
        return ResponseEntity.ok(sugerencias);
    }

    /**
     * Obtiene la trazabilidad completa de un producto.
     * 
     * Endpoint: GET /api/v1/productos/{id}/trazabilidad
     * 
     * Muestra el viaje completo del producto desde su origen hasta la entrega,
     * incluyendo las emisiones de CO₂ por cada etapa.
     * 
     * @param id ID del producto
     * @return ResponseEntity con la trazabilidad completa y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/productos/1/trazabilidad
     * 
     * Ejemplo de respuesta:
     * {
     *   "productoId": 1,
     *   "nombreProducto": "Producto Ejemplo",
     *   "huellaTotalVerificada": 1.2,
     *   "etapas": [
     *     {
     *       "etapa": "Origen",
     *       "pais": "Australia",
     *       "descripcion": "Fabricado con Bambú 100%, 100% reciclable",
     *       "co2": 0.5
     *     },
     *     {
     *       "etapa": "Empaque",
     *       "ubicacion": "Centro de distribución",
     *       "descripcion": "100% materiales reciclables",
     *       "co2": 0.2
     *     },
     *     {
     *       "etapa": "Transporte",
     *       "descripcion": "Logística optimizada (15000 km)",
     *       "co2": 0.4,
     *       "distancia": 15000,
     *       "optimizado": true
     *     },
     *     {
     *       "etapa": "Entrega",
     *       "ubicacion": "Tu hogar",
     *       "descripcion": "Última milla carbono neutral",
     *       "co2": 0.1,
     *       "optimizado": true
     *     }
     *   ]
     * }
     */
    @GetMapping("/{id}/trazabilidad")
    public ResponseEntity<com.ecoshop.dto.Producto.TrazabilidadProductoResponse> obtenerTrazabilidad(
            @PathVariable Integer id) {
        com.ecoshop.dto.Producto.TrazabilidadProductoResponse trazabilidad = 
                impactoAmbientalService.obtenerTrazabilidadProducto(id);
        return ResponseEntity.ok(trazabilidad);
    }

    /**
     * Obtiene el viaje del producto (alias para trazabilidad).
     * 
     * Endpoint: GET /api/v1/productos/{id}/viaje
     * 
     * Este endpoint es un alias de /trazabilidad para mantener compatibilidad
     * con diferentes nombres de endpoints.
     * 
     * @param id ID del producto
     * @return ResponseEntity con la trazabilidad completa y código HTTP 200 (OK)
     */
    @GetMapping("/{id}/viaje")
    public ResponseEntity<com.ecoshop.dto.Producto.TrazabilidadProductoResponse> obtenerViajeProducto(
            @PathVariable Integer id) {
        return obtenerTrazabilidad(id);
    }
}
