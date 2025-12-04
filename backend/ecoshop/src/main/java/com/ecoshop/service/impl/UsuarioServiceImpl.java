package com.ecoshop.service.impl;

import com.ecoshop.domain.Marca;
import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.UsuarioMapper;
import com.ecoshop.repository.MarcaRepository;
import com.ecoshop.repository.PedidoRepository;
import com.ecoshop.repository.UsuarioRepository;
import com.ecoshop.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de usuarios.
 * 
 * Esta clase contiene la lógica de negocio para gestionar usuarios.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Manejar excepciones de negocio
 * - Hashear contraseñas (antes de integrar Clerk)
 * - Sincronizar usuarios con Clerk (cuando se integre)
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (email único, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Hashear contraseña si se proporciona (antes de Clerk)
 * 4. Guardar en la base de datos
 * 5. Convertir entidad a DTO de respuesta usando el mapper
 * 6. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class UsuarioServiceImpl implements UsuarioService {

    // Repositorio para acceder a la base de datos de usuarios
    private final UsuarioRepository usuarioRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final UsuarioMapper usuarioMapper;
    
    // Repositorios para verificar relaciones antes de eliminar
    private final MarcaRepository marcaRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Crea un nuevo usuario en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el email no exista
     * 2. Si se proporciona password, lo hashea (antes de integrar Clerk)
     * 3. Convierte el DTO a entidad usando el mapper
     * 4. Guarda el usuario en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO con el ID asignado
     * 
     * @param dto UsuarioRequestDTO con los datos del usuario a crear
     * @return UsuarioResponseDTO con el usuario creado y su ID asignado
     * @throws BadRequestException si el email ya existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/usuarios
     * Body: { "email": "user@example.com", "nombre": "Juan", "rol": "cliente", ... }
     */
    @Override
    @Transactional
    public UsuarioResponseDTO createUsuario(UsuarioRequestDTO dto) {
        // 1. Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("El email ya está registrado: " + dto.getEmail());
        }

        // 2. Validar que el clerkId no exista si se proporciona
        if (dto.getClerkId() != null && !dto.getClerkId().trim().isEmpty()) {
            if (usuarioRepository.existsByClerkId(dto.getClerkId())) {
                throw new BadRequestException("El clerkId ya está registrado: " + dto.getClerkId());
            }
        }

        // 3. Convertir DTO a entidad usando el mapper
        Usuario usuario = usuarioMapper.toEntity(dto);
        
        // 4. Password handling: Con Clerk integrado, el password es opcional
        // Los usuarios autenticados con Clerk no requieren password_hash local
        // Solo se mantiene para compatibilidad con usuarios legacy si es necesario
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            // NOTA: Con Clerk, este bloque generalmente no se ejecuta ya que los usuarios
            // se crean desde webhooks sin password. Solo se mantiene para casos especiales.
            usuario.setPasswordHash(dto.getPassword()); // TEMPORAL: Solo para casos legacy
        }

        // 5. Guardar en la BD (JPA asigna el ID automáticamente)
        Usuario savedUsuario = usuarioRepository.save(usuario);

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return usuarioMapper.toResponse(savedUsuario);
    }

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
     * @throws ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/usuarios/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public UsuarioResponseDTO getUsuarioById(Integer id) {
        // Buscamos el usuario en la BD
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return usuarioMapper.toResponse(usuario);
    }

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
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<UsuarioResponseDTO> getAllUsuarios() {
        // Obtenemos todos los usuarios de la BD
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return usuarios.stream()
                .map(usuarioMapper::toResponse) // Convierte cada Usuario a UsuarioResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

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
     * @throws ResourceNotFoundException si el usuario no existe
     * 
     * Nota: El password no se actualiza con este método. Con Clerk integrado,
     * el password se maneja completamente en Clerk y no se almacena localmente.
     * 
     * Ejemplo de uso:
     * PUT /api/v1/usuarios/1
     * Body: { "nombre": "Juan Actualizado", "direccionDefault": "Nueva dirección", ... }
     */
    @Override
    @Transactional
    public UsuarioResponseDTO updateUsuario(Integer id, UsuarioRequestDTO dto) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // 2. Validar que el nuevo email no esté en uso por otro usuario
        if (dto.getEmail() != null && !dto.getEmail().equals(existingUsuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("El email ya está registrado: " + dto.getEmail());
            }
        }

        // 3. Validar que el nuevo clerkId no esté en uso por otro usuario
        if (dto.getClerkId() != null && !dto.getClerkId().trim().isEmpty()
                && !dto.getClerkId().equals(existingUsuario.getClerkId())) {
            if (usuarioRepository.existsByClerkId(dto.getClerkId())) {
                throw new BadRequestException("El clerkId ya está registrado: " + dto.getClerkId());
            }
        }

        // 4. Preservamos la fecha de registro original (es inmutable)
        // La fecha de registro no se actualiza, se mantiene con su valor original

        // 5. Actualizamos los campos básicos usando el mapper
        usuarioMapper.updateEntityFromDto(existingUsuario, dto);

        // 6. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Usuario updatedUsuario = usuarioRepository.save(existingUsuario);

        // 7. Convertir entidad a DTO de respuesta usando el mapper
        return usuarioMapper.toResponse(updatedUsuario);
    }

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
     * INTEGRACIÓN CON CLERK:
     * Con Clerk integrado, este método se llama desde ClerkWebhookController cuando
     * Clerk envía un webhook de eliminación de usuario (user.deleted).
     * 
     * @param id Identificador del usuario a eliminar
     * @throws ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/usuarios/1
     */
    @Override
    @Transactional
    public void deleteUsuario(Integer id) {
        // Verificamos que el usuario exista
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + id);
        }
        
        // Verificar si el usuario tiene marcas asociadas
        List<Marca> marcas = marcaRepository.findByUsuario_UsuarioId(id);
        if (!marcas.isEmpty()) {
            throw new BadRequestException(
                    String.format("No se puede eliminar el usuario con id %d porque tiene %d marca(s) asociada(s). " +
                            "Elimina primero las marcas antes de eliminar el usuario.", id, marcas.size())
            );
        }
        
        // Verificar si el usuario tiene pedidos asociados
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(id);
        if (!pedidos.isEmpty()) {
            throw new BadRequestException(
                    String.format("No se puede eliminar el usuario con id %d porque tiene %d pedido(s) asociado(s). " +
                            "Los pedidos históricos no pueden eliminarse. Considera desactivar el usuario en lugar de eliminarlo.", 
                            id, pedidos.size())
            );
        }
        
        // Si no tiene relaciones, procedemos con la eliminación
        usuarioRepository.deleteById(id);
    }
}
