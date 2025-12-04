package com.ecoshop.repository;

import com.ecoshop.domain.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a los datos de marcas en la base de datos.
 * 
 * Esta interfaz extiende JpaRepository que proporciona métodos CRUD básicos
 * sin necesidad de implementarlos manualmente. Spring Data JPA genera
 * automáticamente la implementación en tiempo de ejecución.
 * 
 * Ventajas de usar Spring Data JPA:
 * - No necesitamos escribir código SQL manualmente
 * - Spring genera automáticamente las consultas
 * - Métodos tipo-safe (tipado seguro)
 * - Facilita las pruebas unitarias
 * - Soporte para paginación y ordenamiento
 * 
 * Genéricos:
 * - Marca: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (marca_id)
 */
@Repository // Indica a Spring que esta interfaz es un repositorio (bean de Spring)
public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    
    /**
     * Busca una marca por su nombre oficial.
     * 
     * La búsqueda es case-sensitive. Útil para buscar marcas por nombre.
     * 
     * @param nombreOficial Nombre oficial de la marca
     * @return Optional con la marca encontrada, o vacío si no existe
     */
    Optional<Marca> findByNombreOficial(String nombreOficial);
    
    /**
     * Busca una marca por su nombre oficial sin distinguir mayúsculas/minúsculas.
     * 
     * Útil cuando los nombres pueden venir en diferentes formatos pero representan
     * la misma marca.
     * 
     * @param nombreOficial Nombre oficial de la marca (case-insensitive)
     * @return Optional con la marca encontrada, o vacío si no existe
     */
    Optional<Marca> findByNombreOficialIgnoreCase(String nombreOficial);
    
    /**
     * Verifica si existe una marca con el nombre oficial especificado.
     * 
     * @param nombreOficial Nombre oficial de la marca
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreOficial(String nombreOficial);
    
    /**
     * Verifica si existe una marca con el nombre oficial especificado sin distinguir mayúsculas/minúsculas.
     * 
     * @param nombreOficial Nombre oficial de la marca (case-insensitive)
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreOficialIgnoreCase(String nombreOficial);
    
    /**
     * Busca todas las marcas de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de marcas del usuario
     */
    List<Marca> findByUsuario_UsuarioId(Integer usuarioId);
}
