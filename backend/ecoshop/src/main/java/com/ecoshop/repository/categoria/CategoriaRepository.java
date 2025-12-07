package com.ecoshop.repository.categoria;

import com.ecoshop.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a los datos de categorías en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 * 
 * Genéricos:
 * - Categoria: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (categoria_id)
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /**
     * Busca una categoría por su nombre.
     * 
     * @param nombre Nombre de la categoría a buscar
     * @return Optional con la categoría si existe, o vacío si no existe
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre especificado.
     * 
     * @param nombre Nombre de la categoría a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}

