package com.ecoshop.controller;

import com.ecoshop.config.ClerkProperties;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador temporal para facilitar pruebas de Clerk sin frontend.
 * 
 * IMPORTANTE: Este controlador es SOLO para desarrollo/testing.
 * Debe deshabilitarse o protegerse en producción.
 * 
 * Endpoints:
 * - POST /api/v1/test/clerk/create-user: Crea un usuario en Clerk y en la BD local
 * - GET /api/v1/test/clerk/users: Lista usuarios de Clerk
 * 
 */
@RestController
@RequestMapping("/api/v1/test/clerk")
@RequiredArgsConstructor
@Slf4j
public class ClerkTestController {

    private final ClerkProperties clerkProperties;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Crea un usuario en Clerk y sincroniza con la base de datos local.
     * 
     * Este endpoint:
     * 1. Crea el usuario en Clerk usando la API de Clerk
     * 2. Crea el usuario en la BD local (simulando el webhook)
     * 
     * @param request Datos del usuario a crear
     * @return Información del usuario creado, incluyendo el clerkId
     */
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Validar que la secret key esté configurada
            String secretKey = clerkProperties.getSecretKey();
            if (secretKey == null || secretKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "CLERK_SECRET_KEY no está configurada",
                                "message", "Verifica que la variable de entorno CLERK_SECRET_KEY esté configurada en tu archivo .env"
                        ));
            }

            // Validar campos requeridos
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El email es requerido"));
            }
            if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El firstName es requerido"));
            }
            if (request.getLastName() == null || request.getLastName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El lastName es requerido"));
            }

            // 1. Crear usuario en Clerk usando la API
            Map<String, Object> clerkResult = createUserInClerk(request);
            
            if (clerkResult == null || !clerkResult.containsKey("clerkId")) {
                String errorMessage = clerkResult != null && clerkResult.containsKey("error") 
                        ? (String) clerkResult.get("error")
                        : "No se pudo crear el usuario en Clerk";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", errorMessage,
                                "details", clerkResult != null ? clerkResult : "Error desconocido",
                                "tip", "Verifica que CLERK_SECRET_KEY sea válida y que la API de Clerk esté accesible"
                        ));
            }

            String clerkUserId = (String) clerkResult.get("clerkId");

            // 2. Crear usuario en la BD local
            UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                    .clerkId(clerkUserId)
                    .email(request.getEmail())
                    .nombre(request.getFirstName() + " " + request.getLastName())
                    .rol(request.getRol() != null ? request.getRol() : "cliente")
                    .build();

            UsuarioResponseDTO usuarioCreado = usuarioService.createUsuario(usuarioDTO);

            // 3. Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario creado exitosamente");
            response.put("clerkId", clerkUserId);
            response.put("usuarioId", usuarioCreado.getUsuarioId());
            response.put("email", usuarioCreado.getEmail());
            response.put("nombre", usuarioCreado.getNombre());
            response.put("rol", usuarioCreado.getRol());
            response.put("note", "Para obtener un token JWT, el usuario debe autenticarse a través del frontend de Clerk. " +
                    "O usa la API de Clerk directamente para crear una sesión.");

            log.info("Usuario de prueba creado: clerkId={}, usuarioId={}", clerkUserId, usuarioCreado.getUsuarioId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al crear usuario de prueba", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear usuario: " + e.getMessage()));
        }
    }

    /**
     * Crea un usuario en Clerk usando la API de Clerk.
     * 
     * @param request Datos del usuario
     * @return Map con el clerkId si es exitoso, o con información del error si falla
     */
    private Map<String, Object> createUserInClerk(CreateUserRequest request) {
        // Normalizar la URL base de la API de Clerk
        // CLERK_API_URL debe ser: "https://api.clerk.com" (sin /v1)
        // El /v1 se añade según necesidad (algunos endpoints lo requieren, otros no)
        String baseApiUrl = clerkProperties.getApiUrl();
        if (baseApiUrl == null || baseApiUrl.isEmpty()) {
            baseApiUrl = "https://api.clerk.com";
        }
        // Asegurar que no termine con / ni con /v1
        baseApiUrl = baseApiUrl.trim();
        if (baseApiUrl.endsWith("/")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1);
        }
        if (baseApiUrl.endsWith("/v1")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 3);
        }
        String secretKey = clerkProperties.getSecretKey();

        // Construir el JSON para crear el usuario
        // Formato correcto según la documentación de Clerk API
        Map<String, Object> userData = new HashMap<>();
        
        // email_addresses debe ser un array de objetos, no un array de strings
        // Formato: [{"email_address": "test@example.com"}]
        List<Map<String, String>> emailAddresses = new ArrayList<>();
        Map<String, String> emailObj = new HashMap<>();
        emailObj.put("email_address", request.getEmail());
        emailAddresses.add(emailObj);
        userData.put("email_addresses", emailAddresses);
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userData.put("password", request.getPassword());
        }
        userData.put("first_name", request.getFirstName());
        userData.put("last_name", request.getLastName());
        
        // Saltar validaciones de contraseña para pruebas (evita error de contraseña comprometida)
        userData.put("skip_password_checks", true);
        userData.put("skip_password_requirement", false);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(userData);
        } catch (Exception e) {
            log.error("Error al serializar datos del usuario", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error al preparar datos: " + e.getMessage());
            return errorResult;
        }

        // Intentar primero sin /v1 (formato más reciente de la API de Clerk)
        // La API de Clerk puede usar /users directamente o /v1/users
        String apiUrl = baseApiUrl + "/users";
        Map<String, Object> result = tryCreateUserRequest(apiUrl, secretKey, jsonBody);
        
        // Si falla con 404, intentar con /v1 (compatibilidad con versiones anteriores)
        if (result != null && result.containsKey("httpCode") && 
            (Integer) result.get("httpCode") == 404) {
            log.info("Primer intento falló con 404, intentando con /v1/users...");
            apiUrl = baseApiUrl + "/v1/users";
            result = tryCreateUserRequest(apiUrl, secretKey, jsonBody);
        }
        
        return result;
    }
    
    /**
     * Intenta crear un usuario en Clerk con la URL proporcionada.
     */
    private Map<String, Object> tryCreateUserRequest(String apiUrl, String secretKey, String jsonBody) {
        try {
            log.info("Enviando request a Clerk: URL={}", apiUrl);
            log.debug("Body={}", jsonBody);

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    jsonBody,
                    MediaType.get("application/json; charset=utf-8")
            );

            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + secretKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (!response.isSuccessful()) {
                    log.error("Error al crear usuario en Clerk: HTTP {} - URL: {} - Error: {}", 
                            response.code(), apiUrl, responseBody);
                    
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "Error al crear usuario en Clerk");
                    errorResult.put("httpCode", response.code());
                    errorResult.put("url", apiUrl);
                    errorResult.put("clerkError", responseBody);
                    
                    // Intentar parsear el error de Clerk
                    try {
                        JsonNode errorNode = objectMapper.readTree(responseBody);
                        if (errorNode.has("errors")) {
                            errorResult.put("clerkErrors", errorNode.get("errors"));
                        }
                        if (errorNode.has("message")) {
                            errorResult.put("clerkMessage", errorNode.get("message").asText());
                        }
                    } catch (Exception e) {
                        // Si no se puede parsear, usar el texto plano
                        errorResult.put("clerkErrorRaw", responseBody);
                    }
                    
                    return errorResult;
                }

                // Usuario creado exitosamente
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (!jsonNode.has("id")) {
                    log.error("Respuesta de Clerk no contiene 'id': {}", responseBody);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "Respuesta de Clerk no contiene el ID del usuario");
                    errorResult.put("clerkResponse", responseBody);
                    return errorResult;
                }
                
                String clerkId = jsonNode.get("id").asText();
                log.info("Usuario creado en Clerk exitosamente: {} (URL usada: {})", clerkId, apiUrl);
                
                Map<String, Object> successResult = new HashMap<>();
                successResult.put("clerkId", clerkId);
                return successResult;
            }
        } catch (IOException e) {
            log.error("Error de I/O al comunicarse con Clerk: URL={}", apiUrl, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error de comunicación con Clerk: " + e.getMessage());
            errorResult.put("exception", e.getClass().getSimpleName());
            errorResult.put("url", apiUrl);
            return errorResult;
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario en Clerk: URL={}", apiUrl, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error inesperado: " + e.getMessage());
            errorResult.put("exception", e.getClass().getSimpleName());
            errorResult.put("url", apiUrl);
            return errorResult;
        }
    }

    /**
     * Verifica la configuración de Clerk y la conectividad con la API.
     * 
     * @return Estado de la configuración y conectividad
     */
    @GetMapping("/check-config")
    public ResponseEntity<Map<String, Object>> checkConfig() {
        Map<String, Object> result = new HashMap<>();
        
        String secretKey = clerkProperties.getSecretKey();
        String apiUrl = clerkProperties.getApiUrl();
        
        result.put("secretKeyConfigured", secretKey != null && !secretKey.isEmpty());
        result.put("secretKeyPrefix", secretKey != null && !secretKey.isEmpty() 
                ? secretKey.substring(0, Math.min(10, secretKey.length())) + "..." 
                : "NO CONFIGURADO");
        result.put("apiUrl", apiUrl != null ? apiUrl : "NO CONFIGURADO");
        
        // Intentar hacer una petición de prueba a Clerk
        if (secretKey != null && !secretKey.isEmpty() && apiUrl != null) {
            try {
                // Normalizar la URL base (igual que en createUserInClerk)
                String baseApiUrl = apiUrl.trim();
                if (baseApiUrl.endsWith("/")) {
                    baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1);
                }
                if (baseApiUrl.endsWith("/v1")) {
                    baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 3);
                }
                
                // Intentar primero sin /v1
                String testUrl = baseApiUrl + "/users?limit=1";
                Request testRequest = new Request.Builder()
                        .url(testUrl)
                        .get()
                        .addHeader("Authorization", "Bearer " + secretKey)
                        .build();
                
                try (Response response = httpClient.newCall(testRequest).execute()) {
                    result.put("clerkApiReachable", true);
                    result.put("clerkApiStatus", response.code());
                    result.put("clerkApiMessage", response.isSuccessful() ? "OK" : "Error: " + response.code());
                    result.put("clerkApiUrl", testUrl);
                    
                    // Si falla con 404 y no tiene /v1, intentar con /v1
                    if (response.code() == 404 && !baseApiUrl.endsWith("/v1")) {
                        testUrl = baseApiUrl + "/v1/users?limit=1";
                        testRequest = new Request.Builder()
                                .url(testUrl)
                                .get()
                                .addHeader("Authorization", "Bearer " + secretKey)
                                .build();
                        
                        try (Response responseV1 = httpClient.newCall(testRequest).execute()) {
                            result.put("clerkApiStatus", responseV1.code());
                            result.put("clerkApiMessage", responseV1.isSuccessful() ? "OK (con /v1)" : "Error: " + responseV1.code());
                            result.put("clerkApiUrl", testUrl);
                            
                            if (!responseV1.isSuccessful()) {
                                String errorBody = responseV1.body() != null ? responseV1.body().string() : "Sin detalles";
                                result.put("clerkApiError", errorBody);
                            }
                        }
                    } else if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                        result.put("clerkApiError", errorBody);
                    }
                }
            } catch (Exception e) {
                result.put("clerkApiReachable", false);
                result.put("clerkApiError", e.getMessage());
            }
        } else {
            result.put("clerkApiReachable", false);
            result.put("clerkApiError", "Secret key o API URL no configurados");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Lista usuarios de Clerk (solo para pruebas).
     * 
     * @return Lista de usuarios de Clerk
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listClerkUsers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Normalizar la URL base (igual que en otros métodos)
            String baseApiUrl = clerkProperties.getApiUrl();
            if (baseApiUrl == null || baseApiUrl.isEmpty()) {
                baseApiUrl = "https://api.clerk.com";
            }
            baseApiUrl = baseApiUrl.trim();
            if (baseApiUrl.endsWith("/")) {
                baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1);
            }
            if (baseApiUrl.endsWith("/v1")) {
                baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 3);
            }
            // Intentar primero sin /v1
            String apiUrl = baseApiUrl + "/users?limit=" + limit;
            String secretKey = clerkProperties.getSecretKey();

            if (secretKey == null || secretKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "CLERK_SECRET_KEY no está configurada"));
            }

            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("Authorization", "Bearer " + secretKey)
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                    return ResponseEntity.status(response.code())
                            .body(Map.of("error", "Error al obtener usuarios: " + errorBody));
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", jsonNode);
                
                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            log.error("Error al listar usuarios de Clerk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar usuarios: " + e.getMessage()));
        }
    }

    /**
     * DTO para crear usuarios de prueba.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateUserRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String rol;
    }
}

