package com.ecoshop.service.usuario;

import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar usuarios.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre usuarios.
 * La implementación se encuentra en UsuarioServiceImpl.
 * 
 * Responsabilidades:
 * - Crear nuevos usuarios
 * - Obtener usuarios por ID o todos los usuarios
 * - Actualizar usuarios existentes
 * - Eliminar usuarios
 * - Sincronizar usuarios con Clerk mediante webhooks
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 * 
 * INTEGRACIÓN CON CLERK:
 * ======================
 * Clerk ya está integrado y funcionando. La sincronización de usuarios se realiza mediante:
 * - Webhooks de Clerk (ClerkWebhookController) que llaman a estos métodos
 * - El repositorio proporciona findByClerkId() para buscar usuarios por ID de Clerk
 * 
 * Nota: La autenticación (login, registro, recuperación de contraseña) es manejada
 * por Clerk. Estos métodos gestionan los datos del usuario en la base de datos local.
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el email no exista
     * 2. Si se proporciona password, lo hashea
     * 3. Convierte el DTO a entidad usando el mapper
     * 4. Guarda el usuario en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO con el ID asignado
     * 
     * @param dto UsuarioRequestDTO con los datos del usuario a crear
     * @return UsuarioResponseDTO con el usuario creado y su ID asignado
     * @throws com.ecoshop.exception.BadRequestException si el email ya existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/usuarios
     * Body: { "email": "user@example.com", "nombre": "Juan", "rol": "cliente", ... }
     */
    UsuarioResponseDTO createUsuario(UsuarioRequestDTO dto);

    /**
     * Busca un usuario por su ID.
     * 
     * Proceso:
     * 1. Busca el usuario en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, lo convierte a UsuarioResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador del usuario
     * @return UsuarioResponseDTO del usuario encontrado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/usuarios/1
     */
    UsuarioResponseDTO getUsuarioById(Integer id);

    /**
     * Obtiene todos los usuarios de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Usuario de la BD
     * 2. Convierte cada entidad a UsuarioResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todos los usuarios convertidos a UsuarioResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/usuarios
     */
    List<UsuarioResponseDTO> getAllUsuarios();

    /**
     * Actualiza un usuario existente.
     * 
     * Proceso:
     * 1. Verifica que el usuario exista
     * 2. Preserva la fecha de registro original
     * 3. Actualiza los campos básicos usando el mapper
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID del usuario a actualizar
     * @param dto Nuevos datos del usuario
     * @return UsuarioResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Nota: El password no se actualiza con este método. Con Clerk integrado,
     * el password se maneja completamente en Clerk y no se almacena localmente.
     * 
     * Ejemplo de uso:
     * PUT /api/v1/usuarios/1
     * Body: { "nombre": "Juan Actualizado", "direccionDefault": "Nueva dirección", ... }
     */
    UsuarioResponseDTO updateUsuario(Integer id, UsuarioRequestDTO dto);

    /**
     * Elimina un usuario de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que el usuario exista
     * 2. Elimina el usuario de la BD
     * 
     * Nota: Si el usuario tiene pedidos o marcas asociadas, la eliminación puede fallar
     * debido a restricciones de integridad referencial, a menos que se maneje
     * con CASCADE DELETE o eliminando primero las relaciones.
     * 
     * @param id Identificador del usuario a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/usuarios/1
     */
    void deleteUsuario(Integer id);
}
