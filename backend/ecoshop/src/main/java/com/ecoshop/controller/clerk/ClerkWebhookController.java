package com.ecoshop.controller.clerk;

import com.ecoshop.config.ClerkProperties;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.dto.clerk.ClerkWebhookDTO;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.usuario.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ClerkProperties clerkProperties;
    private final ObjectMapper objectMapper;
    
    /**
     * Tiempo máximo permitido para un webhook (5 minutos en segundos).
     * Esto previene ataques de replay donde un atacante reenvía un webhook antiguo.
     */
    private static final long MAX_WEBHOOK_AGE_SECONDS = 300; // 5 minutos

    /**
     * Recibe webhooks de Clerk.
     * 
     * Este endpoint procesa los eventos de Clerk y sincroniza los usuarios
     * en la base de datos local.
     * 
     * IMPORTANTE - Seguridad:
     * Este endpoint verifica la firma del webhook usando el webhook secret de Clerk
     * para asegurar que el webhook realmente proviene de Clerk y no ha sido modificado.
     * 
     * @param request HttpServletRequest para obtener el body raw y headers
     * @return ResponseEntity con código HTTP 200 si se procesó correctamente
     * 
     */
    @PostMapping
    public ResponseEntity<?> handleWebhook(HttpServletRequest request) {
        try {
            // Leer el body raw del request (necesario para verificar la firma)
            byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
            String bodyString = new String(requestBody, StandardCharsets.UTF_8);
            
            // Extraer el header de firma
            String signatureHeader = request.getHeader("svix-signature");
            String webhookSecret = clerkProperties.getWebhookSecret();
            
            // Verificar la firma del webhook
            if (webhookSecret == null || webhookSecret.isEmpty()) {
                log.warn("CLERK_WEBHOOK_SECRET no configurado. Webhook aceptado sin verificación (SOLO PARA DESARROLLO)");
            } else {
                if (signatureHeader == null || signatureHeader.isEmpty()) {
                    log.warn("Webhook rechazado: header svix-signature faltante");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Missing svix-signature header"));
                }
                
                if (!verifyWebhookSignature(signatureHeader, bodyString, webhookSecret)) {
                    log.warn("Webhook rechazado: firma inválida");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid signature"));
                }
                
                log.debug("Webhook verificado correctamente");
            }
            
            // Deserializar el webhook desde el body
            ClerkWebhookDTO webhook = objectMapper.readValue(bodyString, ClerkWebhookDTO.class);
            log.info("Recibido webhook de Clerk: tipo={}, clerkId={}", webhook.getType(), webhook.getClerkId());

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Verifica la firma de un webhook de Clerk usando el formato Svix.
     * 
     * Clerk usa Svix para firmar webhooks. El formato del header svix-signature es:
     * t=timestamp,v1=signature1 v1=signature2 ...
     * 
     * La verificación requiere:
     * 1. Parsear el timestamp y las firmas del header
     * 2. Verificar que el timestamp no sea muy antiguo (prevenir replay attacks)
     * 3. Calcular HMAC SHA256 del payload con el secret
     * 4. Comparar con las firmas recibidas
     * 
     * @param signatureHeader Header svix-signature del request
     * @param payload Body del request como string
     * @param secret Webhook secret de Clerk (whsec_xxxxx)
     * @return true si la firma es válida, false en caso contrario
     */
    private boolean verifyWebhookSignature(String signatureHeader, String payload, String secret) {
        try {
            // Parsear el header svix-signature
            // Formato: t=timestamp,v1=signature1 v1=signature2 ...
            Map<String, String> signatureParts = parseSignatureHeader(signatureHeader);
            String timestampStr = signatureParts.get("t");
            List<String> signatures = Arrays.stream(signatureParts.get("v1").split(" "))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (timestampStr == null || signatures.isEmpty()) {
                log.warn("Formato de firma inválido: timestamp o firmas faltantes");
                return false;
            }
            
            // Verificar el timestamp (prevenir replay attacks)
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis() / 1000; // Convertir a segundos
            long age = currentTime - timestamp;
            
            if (age < 0) {
                log.warn("Webhook con timestamp futuro: posible manipulación");
                return false;
            }
            
            if (age > MAX_WEBHOOK_AGE_SECONDS) {
                log.warn("Webhook demasiado antiguo ({} segundos): posible replay attack", age);
                return false;
            }
            
            // Extraer el secret real (remover el prefijo "whsec_")
            String secretKey = secret;
            if (secret.startsWith("whsec_")) {
                secretKey = secret.substring(6);
            }
            
            // Calcular la firma esperada
            // Formato: timestamp.payload
            String signedContent = timestampStr + "." + payload;
            String expectedSignature = calculateHmacSha256(signedContent, secretKey);
            
            // Comparar con las firmas recibidas (puede haber múltiples versiones)
            for (String receivedSignature : signatures) {
                if (secureCompare(expectedSignature, receivedSignature)) {
                    log.debug("Firma verificada correctamente");
                    return true;
                }
            }
            
            log.warn("Ninguna firma coincide con la esperada");
            return false;
            
        } catch (Exception e) {
            log.error("Error al verificar firma del webhook", e);
            return false;
        }
    }
    
    /**
     * Parsea el header svix-signature en sus componentes.
     * 
     * Formato esperado: t=timestamp,v1=signature1 v1=signature2 ...
     * 
     * @param signatureHeader Header svix-signature
     * @return Map con las partes parseadas (t, v1)
     */
    private Map<String, String> parseSignatureHeader(String signatureHeader) {
        Map<String, String> parts = new HashMap<>();
        
        // Dividir por comas
        String[] pairs = signatureHeader.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                if (key.equals("t")) {
                    parts.put("t", value);
                } else if (key.equals("v1")) {
                    // Si ya existe v1, concatenar (puede haber múltiples v1)
                    String existing = parts.get("v1");
                    parts.put("v1", existing != null ? existing + " " + value : value);
                }
            }
        }
        
        return parts;
    }
    
    /**
     * Calcula HMAC SHA256 de un mensaje usando una clave secreta.
     * 
     * @param message Mensaje a firmar
     * @param secret Clave secreta
     * @return Firma HMAC SHA256 en formato hexadecimal
     */
    private String calculateHmacSha256(String message, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al calcular HMAC SHA256", e);
        }
    }
    
    /**
     * Convierte un array de bytes a hexadecimal.
     * 
     * @param bytes Array de bytes
     * @return String hexadecimal
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Compara dos strings de forma segura (time-constant) para prevenir timing attacks.
     * 
     * @param a Primer string
     * @param b Segundo string
     * @return true si son iguales, false en caso contrario
     */
    private boolean secureCompare(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
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

