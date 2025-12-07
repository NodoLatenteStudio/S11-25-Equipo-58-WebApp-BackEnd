package com.ecoshop.repository.contenido;

import com.ecoshop.domain.ContenidoEducativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a los datos de contenido educativo en la base de datos.
 */
@Repository
public interface ContenidoEducativoRepository extends JpaRepository<ContenidoEducativo, Integer> {
    
    /**
     * Busca todos los contenidos educativos activos.
     * 
     * @param activo Estado activo del contenido
     * @return Lista de contenidos educativos activos
     */
    List<ContenidoEducativo> findByActivo(Boolean activo);
    
    /**
     * Busca contenidos educativos por categoría.
     * 
     * @param categoria Categoría del contenido
     * @return Lista de contenidos educativos de la categoría
     */
    List<ContenidoEducativo> findByCategoriaIgnoreCase(String categoria);
    
    /**
     * Busca contenidos educativos activos por categoría.
     * 
     * @param categoria Categoría del contenido
     * @param activo Estado activo del contenido
     * @return Lista de contenidos educativos activos de la categoría
     */
    List<ContenidoEducativo> findByCategoriaIgnoreCaseAndActivo(String categoria, Boolean activo);
    
    /**
     * Busca contenidos educativos por título (búsqueda parcial, case-insensitive).
     * 
     * @param titulo Título o parte del título a buscar
     * @return Lista de contenidos educativos que coinciden
     */
    List<ContenidoEducativo> findByTituloContainingIgnoreCase(String titulo);
}

