package com.ecoshop.repository;

import com.ecoshop.domain.Certificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a los datos de certificaciones en la base de datos.
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
 * - Certificacion: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (certificacion_id)
 */
@Repository // Indica a Spring que esta interfaz es un repositorio (bean de Spring)
public interface CertificacionRepository extends JpaRepository<Certificacion, Integer> {
    
    /**
     * Busca una certificación por su nombre de sello.
     * 
     * La búsqueda es case-sensitive. Útil para buscar certificaciones por nombre.
     * 
     * @param nombreSello Nombre del sello de certificación
     * @return Optional con la certificación encontrada, o vacío si no existe
     */
    Optional<Certificacion> findByNombreSello(String nombreSello);
    
    /**
     * Busca una certificación por su nombre de sello sin distinguir mayúsculas/minúsculas.
     * 
     * Útil cuando los nombres pueden venir en diferentes formatos pero representan
     * la misma certificación.
     * 
     * @param nombreSello Nombre del sello de certificación (case-insensitive)
     * @return Optional con la certificación encontrada, o vacío si no existe
     */
    Optional<Certificacion> findByNombreSelloIgnoreCase(String nombreSello);
    
    /**
     * Verifica si existe una certificación con el nombre de sello especificado.
     * 
     * @param nombreSello Nombre del sello de certificación
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreSello(String nombreSello);
    
    /**
     * Verifica si existe una certificación con el nombre de sello especificado sin distinguir mayúsculas/minúsculas.
     * 
     * @param nombreSello Nombre del sello de certificación (case-insensitive)
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreSelloIgnoreCase(String nombreSello);
}

