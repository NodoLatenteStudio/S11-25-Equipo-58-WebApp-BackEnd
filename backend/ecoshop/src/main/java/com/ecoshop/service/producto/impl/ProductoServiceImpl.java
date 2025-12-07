package com.ecoshop.service.producto.impl;

import com.ecoshop.domain.Certificacion;
import com.ecoshop.domain.Marca;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.Producto.ProductoPaginadoResponse;
import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.ProductoMapper;
import com.ecoshop.repository.categoria.CategoriaRepository;
import com.ecoshop.repository.certificacion.CertificacionRepository;
import com.ecoshop.repository.marca.MarcaRepository;
import com.ecoshop.repository.producto.ProductoRepository;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.producto.ProductoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de productos.
 * 
 * Esta clase contiene la lógica de negocio para gestionar productos.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Manejar excepciones de negocio
 * - Gestionar relaciones (marca, certificaciones)
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (marca existe, SKU único, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Asignar relaciones (marca, certificaciones)
 * 4. Guardar en la base de datos
 * 5. Recargar desde la BD para asegurar que las relaciones estén cargadas
 * 6. Convertir entidad a DTO de respuesta usando el mapper
 * 7. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class ProductoServiceImpl implements ProductoService {

    // Repositorio para acceder a la base de datos de productos
    private final ProductoRepository productoRepository;
    
    // Repositorio para acceder a las marcas
    private final MarcaRepository marcaRepository;
    
    // Repositorio para acceder a las categorías
    private final CategoriaRepository categoriaRepository;
    
    // Repositorio para acceder a las certificaciones
    private final CertificacionRepository certificacionRepository;
    
    // Servicio para acceder a categorías
    private final com.ecoshop.service.categoria.CategoriaService categoriaService;
    
    // Mapper para convertir entre entidades y DTOs
    private final ProductoMapper productoMapper;
    
    // EntityManager para operaciones avanzadas de JPA (refresh, flush, etc.)
    private final EntityManager entityManager;
    
    // Servicio para calcular impacto ambiental
    private final ImpactoAmbientalService impactoAmbientalService;

    /**
     * Crea un nuevo producto en la base de datos.
     * 
     * Proceso:
     * 1. Valida que la marca exista
     * 2. Valida que el SKU sea único (si se proporciona)
     * 3. Convierte el DTO a entidad usando el mapper
     * 4. Asigna la marca y las certificaciones
     * 5. Guarda el producto en la BD
     * 6. Recarga el producto para asegurar que las relaciones estén cargadas
     * 7. Convierte la entidad a DTO de respuesta usando el mapper
     * 8. Retorna el DTO con el ID asignado
     * 
     * @param dto ProductoRequestDTO con los datos del producto a crear
     * @return ProductoResponseDTO con el producto creado y su ID asignado
     * @throws ResourceNotFoundException si la marca no existe
     * @throws BadRequestException si el SKU ya existe o si alguna certificación no existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/productos
     * Body: { "marcaId": 1, "nombre": "Botella reutilizable", "precio": 14990, ... }
     */
    @Override
    @Transactional
    public ProductoResponseDTO createProducto(ProductoRequestDTO dto) {
        // 1. Validar existencia de la marca
        Marca marca = marcaRepository.findById(dto.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + dto.getMarcaId()));

        // 2. Validar SKU único si se proporciona
        if (dto.getSku() != null && !dto.getSku().trim().isEmpty()) {
            if (productoRepository.existsBySku(dto.getSku())) {
                throw new BadRequestException("Ya existe un producto con el SKU: " + dto.getSku());
            }
        }

        // 3. Convertir DTO a entidad usando el mapper
        Producto producto = productoMapper.toEntity(dto);
        
        // 4. Asignar la marca y las certificaciones
        producto.setMarca(marca);
        Set<Certificacion> certificaciones = loadCertificacionesByIds(dto.getCertificaciones());
        producto.setCertificaciones(certificaciones);

        // 5. Guardar en la BD (JPA asigna el ID automáticamente)
        Producto savedProducto = productoRepository.save(producto);
        
        // 6. Forzamos el flush para asegurar que los cambios se persistan
        entityManager.flush();
        
        // 7. Recargamos el producto desde la BD para asegurar que las certificaciones se carguen
        Producto refreshedProducto = productoRepository.findById(savedProducto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Error al recargar el producto guardado"));

        // 8. Calcular y almacenar todas las métricas ambientales detalladas
        refreshedProducto = impactoAmbientalService.calcularYAlmacenarMetricasDetalladas(refreshedProducto);
        
        // 9. Calcular y actualizar eco-badge automáticamente
        com.ecoshop.enums.EcoBadge ecoBadge = impactoAmbientalService.calcularEcoBadge(refreshedProducto);
        refreshedProducto.setEcoBadge(ecoBadge != null ? ecoBadge.getValor() : null);
        
        refreshedProducto = productoRepository.save(refreshedProducto);

        // 10. Convertir entidad a DTO de respuesta usando el mapper
        return productoMapper.toResponse(refreshedProducto);
    }

    /**
     * Busca un producto por su ID.
     * 
     * Proceso:
     * 1. Busca el producto en la BD (con certificaciones y marca cargadas)
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, lo convierte a ProductoResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador del producto
     * @return ProductoResponseDTO del producto encontrado
     * @throws ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public ProductoResponseDTO getProductoById(Integer id) {
        // Buscamos el producto en la BD (con certificaciones y marca cargadas por @EntityGraph)
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return productoMapper.toResponse(producto);
    }

    /**
     * Obtiene todos los productos de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Producto de la BD (con certificaciones y marca cargadas)
     * 2. Convierte cada entidad a ProductoResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todos los productos convertidos a ProductoResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<ProductoResponseDTO> getAllProductos() {
        // Obtenemos todos los productos de la BD (con certificaciones y marca cargadas por @EntityGraph)
        List<Producto> productos = productoRepository.findAll();
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return productos.stream()
                .map(productoMapper::toResponse) // Convierte cada Producto a ProductoResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoPaginadoResponse getAllProductosPaginados(Integer page, Integer size) {
        // Valores por defecto: página 1, tamaño 6 (según requerimiento de UX/UI)
        // Convertir página basada en 1 a índice basado en 0 para Spring Data
        int paginaUsuario = (page != null && page >= 1) ? page : 1;
        int paginaInterna = paginaUsuario - 1; // Convertir a índice basado en 0
        int tamano = (size != null && size > 0) ? size : 6;
        
        // Crear Pageable con los parámetros (índice basado en 0)
        Pageable pageable = PageRequest.of(paginaInterna, tamano);
        
        // Obtener página de productos
        Page<Producto> productoPage = productoRepository.findAll(pageable);
        
        // Convertir productos a DTOs
        List<ProductoResponseDTO> productosDTO = productoPage.getContent().stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
        
        // Construir respuesta paginada (página basada en 1 para el usuario)
        return ProductoPaginadoResponse.builder()
                .productos(productosDTO)
                .paginaActual(paginaUsuario) // Página basada en 1
                .tamanoPagina(productoPage.getSize())
                .totalElementos(productoPage.getTotalElements())
                .totalPaginas(productoPage.getTotalPages())
                .tieneSiguiente(productoPage.hasNext())
                .tieneAnterior(productoPage.hasPrevious())
                .esPrimera(productoPage.isFirst())
                .esUltima(productoPage.isLast())
                .build();
    }

    /**
     * Obtiene todos los productos de una marca específica.
     * 
     * Proceso:
     * 1. Busca productos por ID de marca en la BD
     * 2. Convierte cada entidad a ProductoResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @param marcaId Identificador de la marca
     * @return Lista de productos de la marca convertidos a ProductoResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos/marca/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<ProductoResponseDTO> getProductosByMarca(Integer marcaId) {
        // Buscamos productos por marca (con certificaciones cargadas por @EntityGraph)
        List<Producto> productos = productoRepository.findByMarca_MarcaId(marcaId);
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return productos.stream()
                .map(productoMapper::toResponse) // Convierte cada Producto a ProductoResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    /**
     * Obtiene todos los productos de una categoría específica.
     * 
     * Proceso:
     * 1. Valida que la categoría exista
     * 2. Busca productos por ID de categoría en la BD
     * 3. Convierte cada entidad a ProductoResponseDTO usando el mapper
     * 4. Retorna la lista de DTOs
     * 
     * @param categoriaId Identificador de la categoría
     * @return Lista de productos de la categoría convertidos a ProductoResponseDTO
     * @throws ResourceNotFoundException si la categoría no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/productos?categoria=1
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> getProductosByCategoria(Integer categoriaId) {
        // Validar que la categoría existe
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoriaId));
        
        // Buscamos productos por categoría (con certificaciones y marca cargadas por @EntityGraph)
        List<Producto> productos = productoRepository.findByCategoria_CategoriaId(categoriaId);
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return productos.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoPaginadoResponse buscarProductosPaginados(
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
            Integer size) {
        
        // Valores por defecto: página 1, tamaño 6 (según requerimiento de UX/UI)
        // Convertir página basada en 1 a índice basado en 0 para cálculos internos
        int paginaUsuario = (page != null && page >= 1) ? page : 1;
        int paginaInterna = paginaUsuario - 1; // Convertir a índice basado en 0
        int tamano = (size != null && size > 0) ? size : 6;
        
        // Obtener todos los productos primero (con certificaciones y marca cargadas)
        List<Producto> todosProductos = productoRepository.findAll();
        
        // Aplicar filtros (misma lógica que buscarProductos)
        List<Producto> productos = new ArrayList<>(todosProductos);
        
        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && 
                            p.getCategoria().getCategoriaId().equals(categoriaId))
                    .collect(Collectors.toList());
        }
        
        if (impacto != null && !impacto.trim().isEmpty()) {
            String impactoLower = impacto.toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getEcoBadge() != null && 
                            p.getEcoBadge().toLowerCase().equals(impactoLower))
                    .collect(Collectors.toList());
        }
        
        if (marcaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getMarca() != null && 
                            p.getMarca().getMarcaId().equals(marcaId))
                    .collect(Collectors.toList());
        }
        
        if (material != null && !material.trim().isEmpty()) {
            String materialLower = material.toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getMateriales() != null && 
                            p.getMateriales().toLowerCase().contains(materialLower))
                    .collect(Collectors.toList());
        }
        
        if (origen != null && !origen.trim().isEmpty()) {
            String origenLower = origen.toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getOrigen() != null && 
                            p.getOrigen().toLowerCase().equals(origenLower))
                    .collect(Collectors.toList());
        }
        
        if (precioMin != null || precioMax != null) {
            java.math.BigDecimal min = precioMin != null ? precioMin : java.math.BigDecimal.ZERO;
            java.math.BigDecimal max = precioMax != null ? precioMax : new java.math.BigDecimal("999999999");
            productos = productos.stream()
                    .filter(p -> p.getPrecio() != null && 
                            p.getPrecio().compareTo(min) >= 0 && 
                            p.getPrecio().compareTo(max) <= 0)
                    .collect(Collectors.toList());
        }
        
        if (certificacionId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCertificaciones() != null && 
                            p.getCertificaciones().stream()
                                    .anyMatch(c -> c.getCertificacionId().equals(certificacionId)))
                    .collect(Collectors.toList());
        }
        
        // Aplicar ordenamiento
        if (ordenarPor != null && !ordenarPor.trim().isEmpty()) {
            switch (ordenarPor.toLowerCase()) {
                case "precio_asc":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                if (p1.getPrecio() == null && p2.getPrecio() == null) return 0;
                                if (p1.getPrecio() == null) return 1;
                                if (p2.getPrecio() == null) return -1;
                                return p1.getPrecio().compareTo(p2.getPrecio());
                            })
                            .collect(Collectors.toList());
                    break;
                case "precio_desc":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                if (p1.getPrecio() == null && p2.getPrecio() == null) return 0;
                                if (p1.getPrecio() == null) return 1;
                                if (p2.getPrecio() == null) return -1;
                                return p2.getPrecio().compareTo(p1.getPrecio());
                            })
                            .collect(Collectors.toList());
                    break;
                case "impacto_asc":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                if (p1.getHuellaCarbonoTotal() == null && p2.getHuellaCarbonoTotal() == null) return 0;
                                if (p1.getHuellaCarbonoTotal() == null) return 1;
                                if (p2.getHuellaCarbonoTotal() == null) return -1;
                                return p1.getHuellaCarbonoTotal().compareTo(p2.getHuellaCarbonoTotal());
                            })
                            .collect(Collectors.toList());
                    break;
                case "impacto_desc":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                if (p1.getHuellaCarbonoTotal() == null && p2.getHuellaCarbonoTotal() == null) return 0;
                                if (p1.getHuellaCarbonoTotal() == null) return 1;
                                if (p2.getHuellaCarbonoTotal() == null) return -1;
                                return p2.getHuellaCarbonoTotal().compareTo(p1.getHuellaCarbonoTotal());
                            })
                            .collect(Collectors.toList());
                    break;
                default:
                    // Si no se reconoce el criterio, mantener orden original
                    break;
            }
        }
        
        // Calcular metadatos de paginación
        long totalElementos = productos.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamano);
        int inicio = paginaInterna * tamano; // Usar índice basado en 0
        int fin = Math.min(inicio + tamano, productos.size());
        
        // Aplicar paginación manual
        List<Producto> productosPaginados = inicio < productos.size() 
                ? productos.subList(inicio, fin)
                : new ArrayList<>();
        
        // Convertir a DTOs
        List<ProductoResponseDTO> productosDTO = productosPaginados.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
        
        // Construir respuesta paginada (página basada en 1 para el usuario)
        return ProductoPaginadoResponse.builder()
                .productos(productosDTO)
                .paginaActual(paginaUsuario) // Página basada en 1
                .tamanoPagina(tamano)
                .totalElementos(totalElementos)
                .totalPaginas(totalPaginas)
                .tieneSiguiente(paginaUsuario < totalPaginas)
                .tieneAnterior(paginaUsuario > 1)
                .esPrimera(paginaUsuario == 1)
                .esUltima(paginaUsuario >= totalPaginas)
                .build();
    }

    /**
     * Actualiza un producto existente.
     * 
     * Proceso:
     * 1. Verifica que el producto exista
     * 2. Valida y actualiza la marca si se proporciona
     * 3. Valida y actualiza el SKU si se cambia
     * 4. Preserva la fecha de creación original
     * 5. Actualiza los campos básicos usando el mapper
     * 6. Actualiza las certificaciones según los códigos del DTO
     * 7. Guarda los cambios en la BD
     * 8. Recarga el producto para asegurar que las relaciones estén cargadas
     * 9. Convierte la entidad a DTO de respuesta usando el mapper
     * 10. Retorna el DTO actualizado
     * 
     * @param id ID del producto a actualizar
     * @param dto Nuevos datos del producto
     * @return ProductoResponseDTO actualizado
     * @throws ResourceNotFoundException si el producto o la marca no existen
     * @throws BadRequestException si el SKU ya existe o si alguna certificación no existe
     * 
     * Ejemplo de uso:
     * PUT /api/v1/productos/1
     * Body: { "nombre": "Botella actualizada", "precio": 15990, ... }
     */
    @Override
    @Transactional
    public ProductoResponseDTO updateProducto(Integer id, ProductoRequestDTO dto) {
        // 1. Cargamos la entidad existente (gestionada por JPA) con sus certificaciones y marca
        Producto existingProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        // 2. Validar y actualizar marca si se proporciona
        if (dto.getMarcaId() != null && !dto.getMarcaId().equals(existingProducto.getMarca().getMarcaId())) {
            Marca nuevaMarca = marcaRepository.findById(dto.getMarcaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + dto.getMarcaId()));
            existingProducto.setMarca(nuevaMarca);
        }

        // 3. Validar SKU único si se cambia
        if (dto.getSku() != null && !dto.getSku().trim().isEmpty() 
                && !dto.getSku().equals(existingProducto.getSku())) {
            if (productoRepository.existsBySku(dto.getSku())) {
                throw new BadRequestException("Ya existe un producto con el SKU: " + dto.getSku());
            }
            existingProducto.setSku(dto.getSku());
        }

        // 4. Preservamos la fecha de creación original
        java.time.LocalDateTime fechaCreacionOriginal = existingProducto.getFechaCreacion();

        // 5. Actualizamos los campos básicos usando el mapper
        productoMapper.updateEntityFromDto(existingProducto, dto);
        
        // 6. Aseguramos que fechaCreacion se mantenga con su valor original
        existingProducto.setFechaCreacion(fechaCreacionOriginal);

        // 7. Actualizamos las certificaciones solo si se proporcionan explícitamente en el DTO
        // Si dto.getCertificaciones() es null, mantenemos las certificaciones existentes
        if (dto.getCertificaciones() != null) {
            Set<Certificacion> nuevasCertificaciones = loadCertificacionesByIds(dto.getCertificaciones());
            
            // Limpiamos las certificaciones existentes y asignamos las nuevas
            existingProducto.getCertificaciones().clear();
            if (!nuevasCertificaciones.isEmpty()) {
                existingProducto.getCertificaciones().addAll(nuevasCertificaciones);
            }
        }
        // Si dto.getCertificaciones() es null, no hacemos nada y mantenemos las certificaciones actuales

        // 9. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Producto updatedProducto = productoRepository.save(existingProducto);
        
        // 10. Forzamos el flush para asegurar que los cambios se persistan
        entityManager.flush();
        
        // 11. Recargamos para asegurar que las certificaciones se carguen
        Producto refreshedProducto = productoRepository.findById(updatedProducto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Error al recargar el producto actualizado"));

        // 12. Recalcular y almacenar todas las métricas ambientales detalladas
        refreshedProducto = impactoAmbientalService.calcularYAlmacenarMetricasDetalladas(refreshedProducto);
        
        // 13. Recalcular y actualizar eco-badge automáticamente
        com.ecoshop.enums.EcoBadge ecoBadge = impactoAmbientalService.calcularEcoBadge(refreshedProducto);
        refreshedProducto.setEcoBadge(ecoBadge != null ? ecoBadge.getValor() : null);
        
        refreshedProducto = productoRepository.save(refreshedProducto);

        // 14. Convertir entidad a DTO de respuesta usando el mapper
        return productoMapper.toResponse(refreshedProducto);
    }

    /**
     * Elimina un producto de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que el producto exista
     * 2. Elimina las relaciones de certificaciones de la tabla intermedia
     * 3. Elimina el producto de la BD
     * 
     * Nota: Verificamos existencia antes de eliminar para dar un mensaje de error
     * más claro. También podríamos dejar que JPA lance la excepción, pero este
     * enfoque nos da más control sobre el mensaje de error.
     * 
     * @param id Identificador del producto a eliminar
     * @throws ResourceNotFoundException si el producto no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/productos/1
     */
    @Override
    @Transactional
    public void deleteProducto(Integer id) {
        // Verificamos que el producto exista
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        }
        
        // Eliminamos directamente las relaciones de la tabla intermedia usando una consulta nativa
        // Esto es más eficiente y evita problemas con foreign key constraints
        productoRepository.deleteProductoCertifications(id);
        
        // Forzamos el flush para asegurar que las relaciones se eliminen antes de eliminar el producto
        entityManager.flush();
        
        // Ahora eliminamos el producto de forma segura
        // Las relaciones ya fueron eliminadas en el paso anterior
        productoRepository.deleteById(id);
        
        // Forzamos el flush final para asegurar que la eliminación se persista
        entityManager.flush();
    }

    /**
     * Carga las certificaciones desde la base de datos según los IDs proporcionados.
     * 
     * Este método privado encapsula la lógica de carga de certificaciones:
     * 1. Si la lista de IDs es null o vacía, retorna un Set vacío
     * 2. Para cada ID, busca la certificación correspondiente
     * 3. Si algún ID no existe, lanza una excepción BadRequestException
     * 4. Retorna un Set con todas las certificaciones encontradas
     * 
     * Nota: Los IDs pueden venir como Integer o como String (desde el DTO).
     * Este método acepta una lista de Strings y los convierte a Integer.
     * 
     * @param certificacionIds Lista de IDs de certificaciones (pueden ser String o Integer)
     * @return Set de entidades Certificacion correspondientes a los IDs
     * @throws BadRequestException si algún ID de certificación no existe
     */
    private Set<Certificacion> loadCertificacionesByIds(List<String> certificacionIds) {
        // Si no hay IDs, retornamos un Set vacío
        if (certificacionIds == null || certificacionIds.isEmpty()) {
            return new HashSet<>();
        }

        // Creamos un Set para almacenar las certificaciones encontradas
        Set<Certificacion> certificaciones = new HashSet<>();
        
        // Lista para almacenar los IDs que no se encontraron
        List<String> idsNoEncontrados = new ArrayList<>();

        // Iteramos sobre cada ID y buscamos la certificación correspondiente
        for (String idStr : certificacionIds) {
            // Validamos que el ID no sea null ni vacío
            if (idStr == null || idStr.trim().isEmpty()) {
                continue; // Saltamos IDs vacíos o null
            }

            try {
                // Convertimos el String a Integer
                Integer id = Integer.parseInt(idStr.trim());
                
                // Buscamos la certificación por ID
                Optional<Certificacion> certificacionOpt = certificacionRepository.findById(id);
                
                if (certificacionOpt.isPresent()) {
                    // Si existe, la agregamos al Set
                    certificaciones.add(certificacionOpt.get());
                } else {
                    // Si no existe, la agregamos a la lista de IDs no encontrados
                    idsNoEncontrados.add(idStr);
                }
            } catch (NumberFormatException e) {
                // Si el ID no es un número válido, lo agregamos a la lista de no encontrados
                idsNoEncontrados.add(idStr);
            }
        }

        // Si hay IDs que no se encontraron, lanzamos una excepción
        if (!idsNoEncontrados.isEmpty()) {
            String mensaje = idsNoEncontrados.size() == 1
                    ? String.format("Certificación con ID '%s' no encontrada", idsNoEncontrados.get(0))
                    : String.format("Certificaciones con IDs %s no encontradas", idsNoEncontrados);
            throw new BadRequestException(mensaje);
        }

        return certificaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public com.ecoshop.dto.Producto.FiltrosDisponiblesResponse obtenerFiltrosDisponibles() {
        // Obtener todas las categorías
        var categoriasDTO = categoriaService.getAllCategorias();
        var categoriasFiltro = categoriasDTO.stream()
                .map(c -> com.ecoshop.dto.Producto.FiltrosDisponiblesResponse.CategoriaFiltro.builder()
                        .categoriaId(c.getCategoriaId())
                        .nombre(c.getNombre())
                        .build())
                .collect(Collectors.toList());

        // Obtener todas las marcas
        var marcas = marcaRepository.findAll();
        var marcasFiltro = marcas.stream()
                .map(m -> com.ecoshop.dto.Producto.FiltrosDisponiblesResponse.MarcaFiltro.builder()
                        .marcaId(m.getMarcaId())
                        .nombre(m.getNombreOficial())
                        .build())
                .collect(Collectors.toList());

        // Obtener todas las certificaciones
        var certificaciones = certificacionRepository.findAll();
        var certificacionesFiltro = certificaciones.stream()
                .map(c -> com.ecoshop.dto.Producto.FiltrosDisponiblesResponse.CertificacionFiltro.builder()
                        .certificacionId(c.getCertificacionId())
                        .nombreSello(c.getNombreSello())
                        .build())
                .collect(Collectors.toList());

        // Obtener todos los productos para extraer materiales, orígenes y rango de precios
        var productos = productoRepository.findAll();

        // Extraer materiales únicos
        var materiales = productos.stream()
                .filter(p -> p.getMateriales() != null && !p.getMateriales().trim().isEmpty())
                .map(Producto::getMateriales)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Extraer orígenes únicos
        var origenes = productos.stream()
                .filter(p -> p.getOrigen() != null && !p.getOrigen().trim().isEmpty())
                .map(Producto::getOrigen)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Calcular rango de precios
        var precios = productos.stream()
                .filter(p -> p.getPrecio() != null)
                .map(Producto::getPrecio)
                .collect(Collectors.toList());

        java.math.BigDecimal precioMin = precios.isEmpty() ? java.math.BigDecimal.ZERO : 
                precios.stream().min(java.math.BigDecimal::compareTo).orElse(java.math.BigDecimal.ZERO);
        java.math.BigDecimal precioMax = precios.isEmpty() ? java.math.BigDecimal.ZERO : 
                precios.stream().max(java.math.BigDecimal::compareTo).orElse(java.math.BigDecimal.ZERO);

        // Niveles de impacto disponibles
        var nivelesImpacto = List.of("bajo_impacto", "medio_impacto", "neutro");

        // Opciones de ordenamiento
        var opcionesOrdenamiento = List.of("precio_asc", "precio_desc", "impacto_asc", "impacto_desc");

        return com.ecoshop.dto.Producto.FiltrosDisponiblesResponse.builder()
                .categorias(categoriasFiltro)
                .nivelesImpacto(nivelesImpacto)
                .marcas(marcasFiltro)
                .materiales(materiales)
                .origenes(origenes)
                .certificaciones(certificacionesFiltro)
                .rangoPrecio(com.ecoshop.dto.Producto.FiltrosDisponiblesResponse.RangoPrecio.builder()
                        .precioMin(precioMin)
                        .precioMax(precioMax)
                        .build())
                .opcionesOrdenamiento(opcionesOrdenamiento)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoPaginadoResponse buscarProductosPorNombre(String query, Integer page, Integer size) {
        if (query == null || query.trim().isEmpty()) {
            // Retornar respuesta vacía paginada
            return ProductoPaginadoResponse.builder()
                    .productos(List.of())
                    .paginaActual(1)
                    .tamanoPagina(6)
                    .totalElementos(0L)
                    .totalPaginas(0)
                    .tieneSiguiente(false)
                    .tieneAnterior(false)
                    .esPrimera(true)
                    .esUltima(true)
                    .build();
        }
        
        // Valores por defecto: página 1, tamaño 6 (según requerimiento de UX/UI)
        // Convertir página basada en 1 a índice basado en 0 para cálculos internos
        int paginaUsuario = (page != null && page >= 1) ? page : 1;
        int paginaInterna = paginaUsuario - 1; // Convertir a índice basado en 0
        int tamano = (size != null && size > 0) ? size : 6;
        
        // Buscar productos por nombre (búsqueda parcial, case-insensitive)
        List<Producto> todosProductos = productoRepository.findByNombreContainingIgnoreCase(query.trim());
        
        // Calcular metadatos de paginación
        long totalElementos = todosProductos.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamano);
        int inicio = paginaInterna * tamano; // Usar índice basado en 0
        int fin = Math.min(inicio + tamano, todosProductos.size());
        
        // Aplicar paginación manual
        List<Producto> productosPaginados = inicio < todosProductos.size() 
                ? todosProductos.subList(inicio, fin)
                : new ArrayList<>();
        
        // Convertir a DTOs
        List<ProductoResponseDTO> productosDTO = productosPaginados.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
        
        // Construir respuesta paginada (página basada en 1 para el usuario)
        return ProductoPaginadoResponse.builder()
                .productos(productosDTO)
                .paginaActual(paginaUsuario) // Página basada en 1
                .tamanoPagina(tamano)
                .totalElementos(totalElementos)
                .totalPaginas(totalPaginas)
                .tieneSiguiente(paginaUsuario < totalPaginas)
                .tieneAnterior(paginaUsuario > 1)
                .esPrimera(paginaUsuario == 1)
                .esUltima(paginaUsuario >= totalPaginas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.ecoshop.dto.Producto.SugerenciasProductosResponse sugerirProductosSostenibles(Integer productoId, Integer limite) {
        // Buscar el producto original
        Producto productoOriginal = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        // Calcular huella de carbono del producto original
        BigDecimal huellaOriginal = impactoAmbientalService.calcularHuellaCarbonoProducto(productoOriginal);
        
        // Obtener todos los productos activos (excluyendo el original)
        List<Producto> todosProductos = productoRepository.findAll().stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .filter(p -> !p.getProductoId().equals(productoId))
                .collect(Collectors.toList());

        // Filtrar y ordenar productos sugeridos
        List<Producto> productosSugeridos = todosProductos.stream()
                .filter(p -> {
                    // Priorizar productos de la misma categoría (si existe)
                    if (productoOriginal.getCategoria() != null && p.getCategoria() != null) {
                        return p.getCategoria().getCategoriaId().equals(productoOriginal.getCategoria().getCategoriaId());
                    }
                    // Si no hay categoría, incluir todos los productos
                    return true;
                })
                .filter(p -> {
                    // Filtrar productos con menor o igual huella de carbono
                    BigDecimal huella = impactoAmbientalService.calcularHuellaCarbonoProducto(p);
                    return huella.compareTo(huellaOriginal) <= 0;
                })
                .sorted((p1, p2) -> {
                    // Ordenar por:
                    // 1. Eco-badge (bajo_impacto primero)
                    // 2. Huella de carbono (menor primero)
                    // 3. Porcentaje reciclable (mayor primero)
                    // 4. Precio (menor primero)
                    
                    // Comparar eco-badge
                    String badge1 = impactoAmbientalService.calcularEcoBadge(p1).getValor();
                    String badge2 = impactoAmbientalService.calcularEcoBadge(p2).getValor();
                    int badgeCompare = compararEcoBadge(badge1, badge2);
                    if (badgeCompare != 0) return badgeCompare;
                    
                    // Comparar huella de carbono
                    BigDecimal huella1 = impactoAmbientalService.calcularHuellaCarbonoProducto(p1);
                    BigDecimal huella2 = impactoAmbientalService.calcularHuellaCarbonoProducto(p2);
                    int huellaCompare = huella1.compareTo(huella2);
                    if (huellaCompare != 0) return huellaCompare;
                    
                    // Comparar porcentaje reciclable
                    Integer reciclable1 = p1.getPorcentajeReciclable() != null ? p1.getPorcentajeReciclable() : 0;
                    Integer reciclable2 = p2.getPorcentajeReciclable() != null ? p2.getPorcentajeReciclable() : 0;
                    int reciclableCompare = reciclable2.compareTo(reciclable1); // Mayor primero
                    if (reciclableCompare != 0) return reciclableCompare;
                    
                    // Comparar precio
                    if (p1.getPrecio() != null && p2.getPrecio() != null) {
                        return p1.getPrecio().compareTo(p2.getPrecio());
                    }
                    return 0;
                })
                .limit(limite != null && limite > 0 ? limite : 5)
                .collect(Collectors.toList());

        // Si no hay suficientes productos en la misma categoría, agregar productos de otras categorías
        if (productosSugeridos.size() < (limite != null && limite > 0 ? limite : 5)) {
            List<Producto> productosAdicionales = todosProductos.stream()
                    .filter(p -> {
                        // Excluir productos ya sugeridos
                        return !productosSugeridos.stream()
                                .anyMatch(sugerido -> sugerido.getProductoId().equals(p.getProductoId()));
                    })
                    .filter(p -> {
                        // Filtrar productos con menor huella de carbono
                        BigDecimal huella = impactoAmbientalService.calcularHuellaCarbonoProducto(p);
                        return huella.compareTo(huellaOriginal) <= 0;
                    })
                    .sorted((p1, p2) -> {
                        // Mismo ordenamiento que antes
                        String badge1 = impactoAmbientalService.calcularEcoBadge(p1).getValor();
                        String badge2 = impactoAmbientalService.calcularEcoBadge(p2).getValor();
                        int badgeCompare = compararEcoBadge(badge1, badge2);
                        if (badgeCompare != 0) return badgeCompare;
                        
                        BigDecimal huella1 = impactoAmbientalService.calcularHuellaCarbonoProducto(p1);
                        BigDecimal huella2 = impactoAmbientalService.calcularHuellaCarbonoProducto(p2);
                        int huellaCompare = huella1.compareTo(huella2);
                        if (huellaCompare != 0) return huellaCompare;
                        
                        Integer reciclable1 = p1.getPorcentajeReciclable() != null ? p1.getPorcentajeReciclable() : 0;
                        Integer reciclable2 = p2.getPorcentajeReciclable() != null ? p2.getPorcentajeReciclable() : 0;
                        int reciclableCompare = reciclable2.compareTo(reciclable1);
                        if (reciclableCompare != 0) return reciclableCompare;
                        
                        if (p1.getPrecio() != null && p2.getPrecio() != null) {
                            return p1.getPrecio().compareTo(p2.getPrecio());
                        }
                        return 0;
                    })
                    .limit((limite != null && limite > 0 ? limite : 5) - productosSugeridos.size())
                    .collect(Collectors.toList());
            
            productosSugeridos.addAll(productosAdicionales);
        }

        // Convertir a DTOs
        List<ProductoResponseDTO> productosDTO = productosSugeridos.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());

        // Construir criterios de sugerencia
        String criterios = "Productos con menor huella de carbono";
        if (productoOriginal.getCategoria() != null) {
            criterios += ", misma categoría (" + productoOriginal.getCategoria().getNombre() + ")";
        }
        criterios += ", mejor eco-badge y precio similar o menor";

        return com.ecoshop.dto.Producto.SugerenciasProductosResponse.builder()
                .productoOriginalId(productoOriginal.getProductoId())
                .productoOriginalNombre(productoOriginal.getNombre())
                .productosSugeridos(productosDTO)
                .totalSugerencias(productosDTO.size())
                .criterios(criterios)
                .build();
    }

    /**
     * Compara dos eco-badges para ordenamiento.
     * 
     * Orden: bajo_impacto < medio_impacto < neutro
     * 
     * @param badge1 Primer eco-badge
     * @param badge2 Segundo eco-badge
     * @return -1 si badge1 es mejor, 1 si badge2 es mejor, 0 si son iguales
     */
    private int compararEcoBadge(String badge1, String badge2) {
        int valor1 = obtenerValorEcoBadge(badge1);
        int valor2 = obtenerValorEcoBadge(badge2);
        return Integer.compare(valor1, valor2);
    }

    /**
     * Obtiene el valor numérico de un eco-badge para comparación.
     * 
     * @param badge Eco-badge
     * @return Valor numérico (menor = mejor)
     */
    private int obtenerValorEcoBadge(String badge) {
        if (badge == null) return 3; // Sin badge = peor
        switch (badge.toLowerCase()) {
            case "bajo_impacto":
                return 1;
            case "medio_impacto":
                return 2;
            case "neutro":
                return 3;
            default:
                return 3;
        }
    }
}
