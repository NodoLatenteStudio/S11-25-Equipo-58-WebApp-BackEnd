package com.ecoshop.service.impl;

import com.ecoshop.domain.Certificacion;
import com.ecoshop.domain.Marca;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.ProductoMapper;
import com.ecoshop.repository.CertificacionRepository;
import com.ecoshop.repository.MarcaRepository;
import com.ecoshop.repository.ProductoRepository;
import com.ecoshop.service.ProductoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    // Repositorio para acceder a las certificaciones
    private final CertificacionRepository certificacionRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final ProductoMapper productoMapper;
    
    // EntityManager para operaciones avanzadas de JPA (refresh, flush, etc.)
    private final EntityManager entityManager;

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

        // 8. Convertir entidad a DTO de respuesta usando el mapper
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

        // 12. Convertir entidad a DTO de respuesta usando el mapper
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
}
