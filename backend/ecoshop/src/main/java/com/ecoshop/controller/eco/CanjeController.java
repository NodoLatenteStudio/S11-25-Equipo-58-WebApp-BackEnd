package com.ecoshop.controller.eco;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Canje.CanjeRequestDTO;
import com.ecoshop.dto.Canje.CanjeResponseDTO;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.service.eco.CanjeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador REST para gestionar canjes de recompensas.
 * 
 * Este controlador expone los endpoints HTTP para operaciones relacionadas con canjes.
 * Todos los endpoints están bajo la ruta base "/api/v1/canjes".
 * 
 * ✅ Todos los endpoints están funcionando correctamente.
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * - Validar permisos (usuarios solo pueden ver sus propios canjes)
 * 
 * Convenciones REST:
 * - POST /api/v1/canjes: Realizar un canje de recompensa (201 Created) ✅
 * - GET /api/v1/canjes/{id}: Obtener un canje por ID (200 OK) ✅
 * - GET /api/v1/canjes/usuario/{usuarioId}: Obtener canjes de un usuario (200 OK) ✅
 * - GET /api/v1/canjes/usuario/{usuarioId}/estado/{estado}: Obtener canjes por estado (200 OK) ✅
 * - GET /api/v1/canjes/usuario/{usuarioId}/fecha: Obtener canjes por rango de fechas (200 OK) ✅
 * - PATCH /api/v1/canjes/{id}/estado: Actualizar estado de un canje (200 OK) ✅
 */
@RestController
@RequestMapping("/api/v1/canjes")
@RequiredArgsConstructor
public class CanjeController {

    private final CanjeService canjeService;

    /**
     * Realiza un canje de recompensa para el usuario autenticado.
     * 
     * Endpoint: POST /api/v1/canjes
     * ✅ Funcionando correctamente.
     * 
     * Este endpoint:
     * - Valida que la recompensa exista y esté activa
     * - Valida que el usuario tenga suficientes eco-puntos
     * - Valida stock disponible (si aplica)
     * - Descuenta los puntos del usuario
     * - Crea el registro de canje
     * - Actualiza el stock de la recompensa
     * 
     * @param dto Datos del canje (recompensaId)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el canje realizado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/canjes
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "recompensaId": 1
     * }
     */
    @PostMapping
    public ResponseEntity<CanjeResponseDTO> realizarCanje(
            @Valid @RequestBody CanjeRequestDTO dto,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CanjeResponseDTO canje = canjeService.realizarCanje(usuario.getUsuarioId(), dto);
        return new ResponseEntity<>(canje, HttpStatus.CREATED);
    }

    /**
     * Obtiene un canje por su ID.
     * 
     * Endpoint: GET /api/v1/canjes/{id}
     * ✅ Funcionando correctamente.
     * 
     * Validación de permisos: Los usuarios solo pueden ver sus propios canjes,
     * excepto los administradores que pueden ver cualquier canje.
     * 
     * @param id Identificador del canje
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el canje encontrado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el canje no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos
     * 
     * Ejemplo de petición:
     * GET /api/v1/canjes/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanjeResponseDTO> getCanjeById(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CanjeResponseDTO canje = canjeService.getCanjeById(id);
        
        // Validar permisos: solo puede ver sus propios canjes o ser admin
        if (!canje.getUsuarioId().equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para ver este canje");
        }
        
        return ResponseEntity.ok(canje);
    }

    /**
     * Obtiene todos los canjes de un usuario.
     * 
     * Endpoint: GET /api/v1/canjes/usuario/{usuarioId}
     * ✅ Funcionando correctamente.
     * 
     * Validación de permisos: Los usuarios solo pueden ver sus propios canjes,
     * excepto los administradores que pueden ver cualquier usuario.
     * 
     * @param usuarioId ID del usuario
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con la lista de canjes del usuario y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/canjes/usuario/1
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CanjeResponseDTO>> getCanjesByUsuario(
            @PathVariable Integer usuarioId,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede ver sus propios canjes o ser admin
        if (!usuarioId.equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para ver los canjes de este usuario");
        }
        
        return ResponseEntity.ok(canjeService.getCanjesByUsuario(usuarioId));
    }

    /**
     * Obtiene canjes de un usuario por estado.
     * 
     * Endpoint: GET /api/v1/canjes/usuario/{usuarioId}/estado/{estado}
     * ✅ Funcionando correctamente.
     * 
     * Validación de permisos: Los usuarios solo pueden ver sus propios canjes,
     * excepto los administradores que pueden ver cualquier usuario.
     * 
     * @param usuarioId ID del usuario
     * @param estado Estado del canje (pendiente, completado, cancelado, expirado)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con la lista de canjes del usuario con el estado especificado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/canjes/usuario/1/estado/completado
     */
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public ResponseEntity<List<CanjeResponseDTO>> getCanjesByUsuarioAndEstado(
            @PathVariable Integer usuarioId,
            @PathVariable String estado,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede ver sus propios canjes o ser admin
        if (!usuarioId.equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para ver los canjes de este usuario");
        }
        
        return ResponseEntity.ok(canjeService.getCanjesByUsuarioAndEstado(usuarioId, estado));
    }

    /**
     * Obtiene canjes de un usuario en un rango de fechas.
     * 
     * Endpoint: GET /api/v1/canjes/usuario/{usuarioId}/fecha?fechaInicio={date}&fechaFin={date}
     * ✅ Funcionando correctamente.
     * 
     * Validación de permisos: Los usuarios solo pueden ver sus propios canjes,
     * excepto los administradores que pueden ver cualquier usuario.
     * 
     * Formato de fecha: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
     * - Ejemplo: 2024-01-01T00:00:00
     * - Ejemplo: 2024-12-31T23:59:59
     * 
     * Comportamiento:
     * - Si ambos parámetros (fechaInicio y fechaFin) son null: retorna TODOS los canjes del usuario
     * - Si solo fechaInicio es null: usa fecha mínima (1970-01-01T00:00:00) hasta fechaFin
     * - Si solo fechaFin es null: usa fechaInicio hasta la fecha actual
     * - Si ambos están presentes: filtra canjes entre fechaInicio y fechaFin
     * 
     * Para obtener las fechas de tus canjes, primero consulta:
     * GET /api/v1/canjes/usuario/{usuarioId} (sin parámetros de fecha)
     * Esto te mostrará todos tus canjes con sus fechas (campo "fechaCanje")
     * 
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del rango (opcional, formato: YYYY-MM-DDTHH:mm:ss)
     * @param fechaFin Fecha de fin del rango (opcional, formato: YYYY-MM-DDTHH:mm:ss)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con la lista de canjes del usuario en el rango de fechas y código HTTP 200 (OK)
     * 
     * Ejemplos de petición:
     * 1. Obtener todos los canjes (sin filtro de fecha):
     *    GET /api/v1/canjes/usuario/1/fecha
     * 
     * 2. Obtener canjes de todo el año 2024:
     *    GET /api/v1/canjes/usuario/1/fecha?fechaInicio=2024-01-01T00:00:00&fechaFin=2024-12-31T23:59:59
     * 
     * 3. Obtener canjes desde una fecha hasta ahora:
     *    GET /api/v1/canjes/usuario/1/fecha?fechaInicio=2024-01-01T00:00:00
     * 
     * 4. Obtener canjes hasta una fecha específica:
     *    GET /api/v1/canjes/usuario/1/fecha?fechaFin=2024-12-31T23:59:59
     */
    @GetMapping("/usuario/{usuarioId}/fecha")
    public ResponseEntity<List<CanjeResponseDTO>> getCanjesByUsuarioAndFecha(
            @PathVariable Integer usuarioId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede ver sus propios canjes o ser admin
        if (!usuarioId.equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para ver los canjes de este usuario");
        }
        
        // Convertir strings a LocalDateTime, tratando "null" o strings vacíos como null
        LocalDateTime fechaInicioParsed = parseFecha(fechaInicio);
        LocalDateTime fechaFinParsed = parseFecha(fechaFin);
        
        return ResponseEntity.ok(canjeService.getCanjesByUsuarioAndFecha(usuarioId, fechaInicioParsed, fechaFinParsed));
    }

    /**
     * Convierte un string a LocalDateTime, tratando "null" o strings vacíos como null.
     * 
     * @param fechaString String con la fecha en formato ISO 8601 (YYYY-MM-DDTHH:mm:ss) o null/"null"
     * @return LocalDateTime parseado o null si el string es null, "null", o vacío
     */
    private LocalDateTime parseFecha(String fechaString) {
        if (fechaString == null || fechaString.trim().isEmpty() || "null".equalsIgnoreCase(fechaString.trim())) {
            return null;
        }
        
        try {
            // Intentar parsear con formato ISO 8601
            return LocalDateTime.parse(fechaString.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            // Si falla el parseo, retornar null en lugar de lanzar excepción
            // Esto permite que el servicio maneje el null correctamente
            return null;
        }
    }

    /**
     * Actualiza el estado de un canje.
     * 
     * Endpoint: PATCH /api/v1/canjes/{id}/estado?estado={estado}
     * ✅ Funcionando correctamente.
     * 
     * Validación de permisos: Solo administradores pueden actualizar estados de canjes.
     * 
     * @param id ID del canje
     * @param estado Nuevo estado del canje (pendiente, completado, cancelado, expirado)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el canje actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el canje no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no es administrador
     * 
     * Ejemplo de petición:
     * PATCH /api/v1/canjes/1/estado?estado=completado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<CanjeResponseDTO> actualizarEstadoCanje(
            @PathVariable Integer id,
            @RequestParam String estado,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo administradores pueden actualizar estados
        if (!"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("Solo los administradores pueden actualizar el estado de los canjes");
        }
        
        return ResponseEntity.ok(canjeService.actualizarEstadoCanje(id, estado));
    }
}

