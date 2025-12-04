package com.ecoshop.controller;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.dto.clerk.ClerkWebhookDTO;
import com.ecoshop.repository.UsuarioRepository;
import com.ecoshop.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para recibir webhooks de Clerk.
 * 
 * Este controlador maneja los eventos que Clerk envía cuando ocurren cambios
 * en los usuarios (creación, actualización, eliminación).
 * 
 * Endpoints:
 * - POST /api/v1/webhooks/clerk: Recibe webhooks de Clerk
 * 
 * Eventos soportados:
 * - user.created: Crea un usuario en la base de datos local cuando se crea en Clerk
 * - user.updated: Actualiza un usuario en la base de datos local cuando se actualiza en Clerk
 * - user.deleted: Elimina un usuario de la base de datos local cuando se elimina en Clerk
 * 
 */
@RestController
@RequestMapping("/api/v1/webhooks/clerk")
@RequiredArgsConstructor
@Slf4j
public class ClerkWebhookController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Recibe webhooks de Clerk.
     * 
     * Este endpoint procesa los eventos de Clerk y sincroniza los usuarios
     * en la base de datos local.
     * 
     * @param webhook Datos del webhook enviado por Clerk
     * @return ResponseEntity con código HTTP 200 si se procesó correctamente
     * 
     * IMPORTANTE - Seguridad:
     * TODO (CRÍTICO PARA PRODUCCIÓN): Implementar verificación de firma del webhook usando webhook secret.
     * Actualmente los webhooks se aceptan sin verificación, lo cual es una vulnerabilidad de seguridad.
     * Se debe implementar la verificación usando Svix o similar antes de desplegar a producción.
     * 
     */
    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody ClerkWebhookDTO webhook) {
        log.info("Recibido webhook de Clerk: tipo={}, clerkId={}", webhook.getType(), webhook.getClerkId());

        try {
            // TODO (CRÍTICO): Verificar la firma del webhook usando el webhook secret antes de procesar
            // Implementación sugerida:
            // String signature = request.getHeader("svix-signature");
            // if (!verifyWebhookSignature(signature, requestBody, clerkProperties.getWebhookSecret())) {
            //     log.warn("Webhook rechazado: firma inválida");
            //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            // }

            Map<String, Object> response = new HashMap<>();
            
            switch (webhook.getType()) {
                case "user.created":
                    UsuarioResponseDTO usuarioCreado = handleUserCreated(webhook);
                    response.put("success", true);
                    response.put("message", "Usuario creado correctamente");
                    response.put("event", "user.created");
                    response.put("usuario", usuarioCreado);
                    return ResponseEntity.ok(response);
                    
                case "user.updated":
                    UsuarioResponseDTO usuarioActualizado = handleUserUpdated(webhook);
                    response.put("success", true);
                    response.put("message", "Usuario actualizado correctamente");
                    response.put("event", "user.updated");
                    response.put("usuario", usuarioActualizado);
                    return ResponseEntity.ok(response);
                    
                case "user.deleted":
                    Integer usuarioIdEliminado = handleUserDeleted(webhook);
                    response.put("success", true);
                    response.put("message", "Usuario eliminado correctamente");
                    response.put("event", "user.deleted");
                    response.put("usuarioId", usuarioIdEliminado);
                    response.put("clerkId", webhook.getClerkId());
                    return ResponseEntity.ok(response);
                    
                default:
                    log.warn("Tipo de webhook no manejado: {}", webhook.getType());
                    response.put("success", false);
                    response.put("message", "Tipo de webhook no manejado: " + webhook.getType());
                    response.put("event", webhook.getType());
                    return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Error al procesar webhook de Clerk", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al procesar webhook: " + e.getMessage());
            errorResponse.put("errorType", e.getClass().getSimpleName());
            if (webhook != null && webhook.getType() != null) {
                errorResponse.put("event", webhook.getType());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Maneja el evento user.created de Clerk.
     * 
     * Crea un usuario en la base de datos local cuando se crea en Clerk.
     * 
     * @param webhook Datos del webhook
     * @return UsuarioResponseDTO del usuario creado o actualizado
     */
    private UsuarioResponseDTO handleUserCreated(ClerkWebhookDTO webhook) {
        String clerkId = webhook.getClerkId();
        String email = webhook.getEmail();
        String nombre = webhook.getFullName();

        if (clerkId == null || email == null) {
            log.warn("Webhook user.created sin clerkId o email: {}", webhook);
            throw new IllegalArgumentException("Webhook user.created sin clerkId o email");
        }

        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByClerkId(clerkId)) {
            log.info("Usuario ya existe con clerkId: {}, actualizando en lugar de crear", clerkId);
            return handleUserUpdated(webhook);
        }

        // Crear el usuario
        UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .nombre(nombre)
                .rol("cliente") // Rol por defecto, puede ajustarse según tus necesidades
                .build();

        UsuarioResponseDTO usuarioCreado = usuarioService.createUsuario(usuarioDTO);
        log.info("Usuario creado desde webhook de Clerk: usuarioId={}, clerkId={}", 
                usuarioCreado.getUsuarioId(), clerkId);
        return usuarioCreado;
    }

    /**
     * Maneja el evento user.updated de Clerk.
     * 
     * Actualiza un usuario en la base de datos local cuando se actualiza en Clerk.
     * 
     * @param webhook Datos del webhook
     * @return UsuarioResponseDTO del usuario actualizado o creado
     */
    private UsuarioResponseDTO handleUserUpdated(ClerkWebhookDTO webhook) {
        String clerkId = webhook.getClerkId();
        String email = webhook.getEmail();
        String nombre = webhook.getFullName();

        if (clerkId == null) {
            log.warn("Webhook user.updated sin clerkId: {}", webhook);
            throw new IllegalArgumentException("Webhook user.updated sin clerkId");
        }

        // Buscar el usuario por clerkId
        Usuario usuario = usuarioRepository.findByClerkId(clerkId).orElse(null);

        if (usuario == null) {
            log.warn("Usuario no encontrado con clerkId: {}, creando nuevo usuario", clerkId);
            return handleUserCreated(webhook);
        }

        // Actualizar el usuario
        UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                .email(email != null ? email : usuario.getEmail())
                .nombre(nombre != null ? nombre : usuario.getNombre())
                .rol(usuario.getRol()) // Mantener el rol actual
                .direccionDefault(usuario.getDireccionDefault()) // Mantener la dirección actual
                .build();

        UsuarioResponseDTO usuarioActualizado = usuarioService.updateUsuario(usuario.getUsuarioId(), usuarioDTO);
        log.info("Usuario actualizado desde webhook de Clerk: usuarioId={}, clerkId={}", 
                usuario.getUsuarioId(), clerkId);
        return usuarioActualizado;
    }

    /**
     * Maneja el evento user.deleted de Clerk.
     * 
     * Elimina un usuario de la base de datos local cuando se elimina en Clerk.
     * 
     * @param webhook Datos del webhook
     * @return ID del usuario eliminado, o null si no se encontró
     */
    private Integer handleUserDeleted(ClerkWebhookDTO webhook) {
        String clerkId = webhook.getClerkId();

        if (clerkId == null) {
            log.warn("Webhook user.deleted sin clerkId: {}", webhook);
            throw new IllegalArgumentException("Webhook user.deleted sin clerkId");
        }

        // Buscar el usuario por clerkId
        Usuario usuario = usuarioRepository.findByClerkId(clerkId).orElse(null);

        if (usuario == null) {
            log.warn("Usuario no encontrado con clerkId: {}, ya fue eliminado", clerkId);
            return null;
        }

        Integer usuarioId = usuario.getUsuarioId();
        // Eliminar el usuario
        usuarioService.deleteUsuario(usuarioId);
        log.info("Usuario eliminado desde webhook de Clerk: usuarioId={}, clerkId={}", usuarioId, clerkId);
        return usuarioId;
    }
}

