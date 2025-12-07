package com.ecoshop.service.pedido.impl;

import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Pedido.PedidoRequestDTO;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.PedidoMapper;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.usuario.EcoPuntosService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.pedido.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de pedidos.
 * 
 * Esta clase contiene la lógica de negocio para gestionar pedidos.
 * Actúa como intermediario entre el controlador (capa de presentación) y
 * el repositorio (capa de acceso a datos).
 * 
 * Responsabilidades:
 * - Convertir entre DTOs y entidades usando el mapper
 * - Manejar transacciones de base de datos
 * - Validar existencia de recursos antes de operaciones
 * - Manejar excepciones de negocio
 * - Gestionar relaciones (usuario)
 * - Validar estados de pedidos
 * 
 * @Transactional: Todas las operaciones de escritura están dentro de una transacción
 * para garantizar la integridad de los datos. Si ocurre un error, se hace rollback.
 * 
 * Flujo típico de operaciones:
 * 1. Validar datos de entrada (usuario existe, estado válido, etc.)
 * 2. Convertir DTO a entidad usando el mapper
 * 3. Asignar relaciones (usuario)
 * 4. Guardar en la base de datos
 * 5. Convertir entidad a DTO de respuesta usando el mapper
 * 6. Retornar el DTO al controlador
 */
@Service // Indica a Spring que esta clase es un componente de servicio (bean de Spring)
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
@Transactional // Todas las operaciones de escritura se ejecutan en una transacción
public class PedidoServiceImpl implements PedidoService {

    // Repositorio para acceder a la base de datos de pedidos
    private final PedidoRepository pedidoRepository;
    
    // Repositorio para acceder a los usuarios
    private final UsuarioRepository usuarioRepository;
    
    // Mapper para convertir entre entidades y DTOs
    private final PedidoMapper pedidoMapper;
    
    // Servicio para calcular impacto ambiental
    private final ImpactoAmbientalService impactoAmbientalService;
    
    // Servicio para gestionar eco-puntos
    private final EcoPuntosService ecoPuntosService;

    /**
     * Crea un nuevo pedido en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Convierte el DTO a entidad usando el mapper
     * 3. Asigna la relación con Usuario
     * 4. Inicializa el total en cero (se calculará a partir de los items)
     * 5. Guarda el pedido en la BD
     * 6. Convierte la entidad a DTO de respuesta usando el mapper
     * 7. Retorna el DTO con el ID asignado
     * 
     * @param dto PedidoRequestDTO con los datos del pedido a crear
     * @return PedidoResponseDTO con el pedido creado y su ID asignado
     * @throws ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/pedidos
     * Body: { "usuarioId": 1, "direccionEnvio": "Av. Principal 123", ... }
     */
    @Override
    @Transactional
    public PedidoResponseDTO createPedido(PedidoRequestDTO dto) {
        // 1. Validar existencia del usuario
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        // 1.5. Validar que direccionEnvio esté presente al crear
        if (dto.getDireccionEnvio() == null || dto.getDireccionEnvio().trim().isEmpty()) {
            throw new BadRequestException("La dirección de envío es obligatoria al crear un pedido");
        }

        // 2. Convertir DTO a entidad usando el mapper
        Pedido pedido = pedidoMapper.toEntity(dto);
        
        // 3. Asignar la relación con Usuario
        pedido.setUsuario(usuario);

        // 4. Guardar en la BD (JPA asigna el ID automáticamente)
        Pedido savedPedido = pedidoRepository.save(pedido);

        // 5. Calcular y actualizar huella de carbono automáticamente
        BigDecimal huellaCarbono = impactoAmbientalService.calcularHuellaCarbonoPedido(savedPedido);
        savedPedido.setHuellaCarbonoTotalKg(huellaCarbono);
        savedPedido = pedidoRepository.save(savedPedido);

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return pedidoMapper.toResponse(savedPedido);
    }

    /**
     * Busca un pedido por su ID.
     * 
     * Proceso:
     * 1. Busca el pedido en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, lo convierte a PedidoResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador del pedido
     * @return PedidoResponseDTO del pedido encontrado
     * @throws ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public PedidoResponseDTO getPedidoById(Integer id) {
        // Buscamos el pedido en la BD
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        
        // Convertimos la entidad a DTO de respuesta usando el mapper
        return pedidoMapper.toResponse(pedido);
    }

    /**
     * Obtiene todos los pedidos de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Pedido de la BD
     * 2. Convierte cada entidad a PedidoResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todos los pedidos convertidos a PedidoResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<PedidoResponseDTO> getAllPedidos() {
        // Obtenemos todos los pedidos de la BD
        List<Pedido> pedidos = pedidoRepository.findAll();
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return pedidos.stream()
                .map(pedidoMapper::toResponse) // Convierte cada Pedido a PedidoResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    /**
     * Obtiene todos los pedidos de un usuario específico.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Busca pedidos por ID de usuario en la BD
     * 3. Convierte cada entidad a PedidoResponseDTO usando el mapper
     * 4. Retorna la lista de DTOs
     * 
     * @param usuarioId Identificador del usuario
     * @return Lista de pedidos del usuario convertidos a PedidoResponseDTO
     * @throws ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/pedidos/usuario/1
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, no necesita transacción de escritura
    public List<PedidoResponseDTO> getPedidosByUsuario(Integer usuarioId) {
        // Validamos que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
        }
        
        // Buscamos pedidos por usuario
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(usuarioId);
        
        // Convertimos cada entidad a DTO de respuesta usando el mapper
        return pedidos.stream()
                .map(pedidoMapper::toResponse) // Convierte cada Pedido a PedidoResponseDTO
                .collect(Collectors.toList()); // Recopila los resultados en una lista
    }

    /**
     * Actualiza el estado de un pedido existente.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Valida que el nuevo estado sea válido
     * 3. Actualiza el estado del pedido
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID del pedido a actualizar
     * @param nuevoEstado Nuevo estado del pedido (pendiente_pago, procesando, enviado, entregado, cancelado)
     * @return PedidoResponseDTO actualizado
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws BadRequestException si el estado no es válido
     * 
     * Ejemplo de uso:
     * PATCH /api/v1/pedidos/1/estado?estado=enviado
     */
    @Override
    @Transactional
    public PedidoResponseDTO updateEstadoPedido(Integer id, String nuevoEstado) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        // 2. Validamos que el nuevo estado sea válido
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }
        
        String estadoNormalizado = nuevoEstado.trim().toLowerCase();
        if (!isEstadoValido(estadoNormalizado)) {
            throw new BadRequestException(
                    String.format("Estado inválido: '%s'. Valores permitidos: pendiente_pago, procesando, enviado, entregado, cancelado", 
                            nuevoEstado));
        }

        // 3. Actualizamos el estado
        pedido.setEstado(estadoNormalizado);

        // 4. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Pedido updatedPedido = pedidoRepository.save(pedido);

        // 5. Si el pedido se marca como "entregado", asignar eco-puntos automáticamente
        if ("entregado".equalsIgnoreCase(estadoNormalizado)) {
            ecoPuntosService.asignarPuntosPorPedido(updatedPedido);
        }

        // 6. Convertir entidad a DTO de respuesta usando el mapper
        return pedidoMapper.toResponse(updatedPedido);
    }

    /**
     * Elimina un pedido de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Elimina el pedido de la BD
     * 
     * Nota: Si el pedido tiene PedidoItems asociados, la eliminación puede fallar
     * debido a restricciones de integridad referencial, a menos que se maneje
     * con CASCADE DELETE o eliminando primero los items.
     * 
     * @param id Identificador del pedido a eliminar
     * @throws ResourceNotFoundException si el pedido no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/pedidos/1
     */
    @Override
    @Transactional
    public void deletePedido(Integer id) {
        // Verificamos que el pedido exista
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + id);
        }
        
        // Eliminamos el pedido de la BD
        pedidoRepository.deleteById(id);
    }

    /**
     * Actualiza un pedido existente completamente.
     * 
     * Proceso:
     * 1. Verifica que el pedido exista
     * 2. Valida que el usuario exista (si se proporciona en el DTO)
     * 3. Preserva el total y fecha de pedido originales
     * 4. Actualiza los campos básicos usando el mapper
     * 5. Actualiza la relación con Usuario si se proporciona usuarioId
     * 6. Guarda los cambios en la BD
     * 7. Convierte la entidad a DTO de respuesta usando el mapper
     * 8. Retorna el DTO actualizado
     * 
     * @param id ID del pedido a actualizar
     * @param dto Nuevos datos del pedido
     * @return PedidoResponseDTO actualizado
     * @throws ResourceNotFoundException si el pedido o usuario no existen
     * 
     * Ejemplo de uso:
     * PUT /api/v1/pedidos/1
     * Body: { "direccionEnvio": "Nueva dirección", "estado": "enviado", ... }
     */
    @Override
    @Transactional
    public PedidoResponseDTO updatePedido(Integer id, PedidoRequestDTO dto) {
        // 1. Cargamos la entidad existente (gestionada por JPA)
        Pedido existingPedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        // 2. Si se proporciona usuarioId, validar y actualizar la relación
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));
            existingPedido.setUsuario(usuario);
        }

        // 3. Preservamos el total y fecha de pedido originales (son inmutables o calculados)
        // La fecha de pedido no se actualiza (es inmutable)
        // El total se calcula a partir de los items, no se actualiza desde el DTO

        // 4. Actualizamos los campos básicos usando el mapper
        pedidoMapper.updateEntityFromDto(existingPedido, dto);

        // 5. Guardamos los cambios (JPA detecta automáticamente los cambios en la entidad gestionada)
        Pedido updatedPedido = pedidoRepository.save(existingPedido);

        // 6. Recalcular y actualizar huella de carbono automáticamente
        BigDecimal huellaCarbono = impactoAmbientalService.calcularHuellaCarbonoPedido(updatedPedido);
        updatedPedido.setHuellaCarbonoTotalKg(huellaCarbono);
        updatedPedido = pedidoRepository.save(updatedPedido);

        // 7. Convertir entidad a DTO de respuesta usando el mapper
        return pedidoMapper.toResponse(updatedPedido);
    }

    /**
     * Valida si un estado de pedido es válido.
     * 
     * Este método privado encapsula la lógica de validación de estados.
     * 
     * @param estado Estado a validar (debe estar en minúsculas)
     * @return true si el estado es válido, false en caso contrario
     */
    private boolean isEstadoValido(String estado) {
        return estado.equals("pendiente_pago") 
                || estado.equals("procesando") 
                || estado.equals("enviado") 
                || estado.equals("entregado") 
                || estado.equals("cancelado");
    }
}
