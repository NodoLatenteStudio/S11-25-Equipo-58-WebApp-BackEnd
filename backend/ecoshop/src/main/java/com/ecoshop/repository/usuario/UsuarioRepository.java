package com.ecoshop.repository.usuario;

import com.ecoshop.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a los datos de usuarios en la base de datos.
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
 * - Usuario: Tipo de entidad
 * - Integer: Tipo del ID de la entidad (usuario_id)
 * 
 */
@Repository // Indica a Spring que esta interfaz es un repositorio (bean de Spring)
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    /**
     * Verifica si existe un usuario con el email especificado.
     * 
     * @param email Email del usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca un usuario por su email.
     * 
     * Útil para autenticación y búsqueda de usuarios.
     * 
     * @param email Email del usuario
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el clerkId especificado.
     * 
     * Con Clerk integrado, este método verifica si un usuario de Clerk
     * ya está sincronizado en la base de datos local.
     * 
     * @param clerkId ID del usuario en Clerk
     * @return true si existe, false en caso contrario
     */
    boolean existsByClerkId(String clerkId);
    
    /**
     * Busca un usuario por su clerkId.
     * 
     * Con Clerk integrado, este método es esencial. Se usa para:
     * - Buscar usuarios cuando se valida un token JWT de Clerk
     * - Sincronizar datos desde webhooks de Clerk
     * - Vincular usuarios locales con usuarios de Clerk
     * 
     * @param clerkId ID del usuario en Clerk
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> findByClerkId(String clerkId);
    
    /**
     * Busca usuarios por rol.
     * 
     * Útil para filtrar usuarios por su rol (cliente, marca, admin).
     * 
     * @param rol Rol del usuario
     * @return Lista de usuarios con el rol especificado
     */
    java.util.List<Usuario> findByRol(String rol);
}
