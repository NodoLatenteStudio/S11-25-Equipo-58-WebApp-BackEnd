package com.ecoshop.repository.producto;

import com.ecoshop.domain.Producto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a los datos de productos en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    /**
     * Obtiene todos los productos con sus certificaciones cargadas.
     * 
     * @EntityGraph: Especifica que se deben cargar las certificaciones
     * junto con los productos en una sola consulta, evitando el problema
     * N+1 y las referencias circulares.
     */
    @Override
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findAll();
    
    /**
     * Obtiene todos los productos con paginación y sus certificaciones cargadas.
     * 
     * @param pageable Información de paginación (página, tamaño, ordenamiento)
     * @return Página de productos con sus certificaciones y marca cargadas
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    Page<Producto> findAll(Pageable pageable);
    
    /**
     * Obtiene un producto por su ID con sus certificaciones y marca cargadas.
     * 
     * @EntityGraph: Especifica que se deben cargar las certificaciones
     * junto con el producto en una sola consulta.
     */
    @Override
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    Optional<Producto> findById(Integer id);
    
    /**
     * Busca productos por ID de marca.
     */
    @EntityGraph(attributePaths = {"certificaciones"})
    List<Producto> findByMarca_MarcaId(Integer marcaId);

    /**
     * Busca productos por ID de categoría.
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByCategoria_CategoriaId(Integer categoriaId);

    /**
     * Busca productos por eco badge (impacto).
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByEcoBadgeIgnoreCase(String ecoBadge);

    /**
     * Busca productos que contengan el material especificado en el campo materiales.
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByMaterialesContainingIgnoreCase(String material);

    /**
     * Busca productos por origen.
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByOrigenIgnoreCase(String origen);

    /**
     * Busca productos por rango de precio.
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Busca productos que tengan una certificación específica.
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    @Query("SELECT DISTINCT p FROM Producto p JOIN p.certificaciones c WHERE c.certificacionId = :certificacionId")
    List<Producto> findByCertificaciones_CertificacionId(@Param("certificacionId") Integer certificacionId);

    /**
     * Verifica si existe un producto con el SKU dado.
     * 
     * @param sku Código SKU a verificar
     * @return true si existe un producto con ese SKU, false en caso contrario
     */
    boolean existsBySku(String sku);
    
    /**
     * Elimina todas las relaciones de certificaciones para un producto específico.
     * 
     * Este método elimina directamente las filas de la tabla intermedia
     * producto_certificaciones para un producto dado.
     * 
     * @param productoId ID del producto cuyas relaciones se eliminarán
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM producto_certificaciones WHERE producto_id = :productoId", nativeQuery = true)
    void deleteProductoCertifications(@Param("productoId") Integer productoId);
    
    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con el nombre
     */
    @EntityGraph(attributePaths = {"certificaciones", "marca"})
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
