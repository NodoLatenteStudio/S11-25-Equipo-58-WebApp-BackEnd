package com.ecoshop.controller.clerk;

import com.ecoshop.config.ClerkProperties;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.usuario.UsuarioService;
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
import org.springframework.transaction.annotation.Transactional;
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
 * - POST /api/v1/test/clerk/assign-eco-puntos: Asigna eco-puntos directamente a un usuario (solo para pruebas)
 * 
 */
@RestController
@RequestMapping("/api/v1/test/clerk")
@RequiredArgsConstructor
@Slf4j
public class ClerkTestController {

    private final ClerkProperties clerkProperties;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Endpoint de prueba simple para verificar que los endpoints públicos funcionan.
     * 
     * @return Mensaje de confirmación
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
                "message", "Endpoint público funcionando correctamente",
                "timestamp", System.currentTimeMillis(),
                "clerkConfigured", clerkProperties.getSecretKey() != null && !clerkProperties.getSecretKey().isEmpty()
        ));
    }

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
        // CLERK_API_URL puede venir con o sin /v1
        // Necesitamos asegurarnos de usar la URL correcta
        String baseApiUrl = clerkProperties.getApiUrl();
        if (baseApiUrl == null || baseApiUrl.isEmpty()) {
            baseApiUrl = "https://api.clerk.com";
        }
        // Normalizar: quitar trailing slash y /v1 si existe
        baseApiUrl = baseApiUrl.trim();
        if (baseApiUrl.endsWith("/")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1);
        }
        if (baseApiUrl.endsWith("/v1")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 3);
        }
        // Ahora baseApiUrl es: https://api.clerk.com (sin /v1)
        String secretKey = clerkProperties.getSecretKey();

        // Construir el JSON para crear el usuario
        // FORMATO CORRECTO de Clerk API:
        // - email_address (singular) como array de strings: ["email@example.com"]
        // - NO usar email_addresses (plural) como array de objetos
        Map<String, Object> userData = new HashMap<>();
        
        // Formato CORRECTO: email_address (singular) como array de strings
        List<String> emailAddressList = new ArrayList<>();
        emailAddressList.add(request.getEmail());
        userData.put("email_address", emailAddressList);
        
        // Generar username desde email (puede ser requerido por Clerk)
        String username = request.getEmail().split("@")[0];
        userData.put("username", username);
        
        // Password puede ser requerido según la configuración
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userData.put("password", request.getPassword());
        } else {
            // Si no se proporciona password, usar uno temporal
            userData.put("password", "TempPassword123!");
        }
        
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            userData.put("first_name", request.getFirstName());
        }
        
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            userData.put("last_name", request.getLastName());
        }
        
        // Saltar validaciones para pruebas
        userData.put("skip_password_checks", true);
        userData.put("skip_email_verification", true);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(userData);
        } catch (Exception e) {
            log.error("Error al serializar datos del usuario", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error al preparar datos: " + e.getMessage());
            return errorResult;
        }

        // Intentar primero con /v1/users (formato estándar de Clerk API)
        // La API de Clerk usa /v1/users como endpoint estándar
        String apiUrl = baseApiUrl + "/v1/users";
        Map<String, Object> result = tryCreateUserRequest(apiUrl, secretKey, jsonBody);
        
        // Si falla con 404, intentar sin /v1 (algunas versiones pueden usar /users directamente)
        if (result != null && result.containsKey("httpCode") && 
            (Integer) result.get("httpCode") == 404) {
            log.info("Primer intento falló con 404, intentando sin /v1...");
            apiUrl = baseApiUrl + "/users";
            result = tryCreateUserRequest(apiUrl, secretKey, jsonBody);
        }
        
        // Si falla con 422 o 400, el formato ya es correcto, así que solo logueamos el error
        if (result != null && result.containsKey("httpCode") && 
            ((Integer) result.get("httpCode") == 422 || (Integer) result.get("httpCode") == 400)) {
            log.warn("Error al crear usuario en Clerk: HTTP {} - {}", result.get("httpCode"), result.get("clerkError"));
        }
        
        return result;
    }
    
    /**
     * Intenta crear un usuario en Clerk con la URL proporcionada.
     */
    private Map<String, Object> tryCreateUserRequest(String apiUrl, String secretKey, String jsonBody) {
        try {
            // Validar que la secret key esté presente y tenga el formato correcto
            if (secretKey == null || secretKey.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "CLERK_SECRET_KEY no está configurada o está vacía");
                errorResult.put("httpCode", 401);
                return errorResult;
            }
            
            // Validar que la secret key tenga el formato esperado (sk_test_ o sk_live_)
            String trimmedKey = secretKey.trim();
            if (!trimmedKey.startsWith("sk_test_") && !trimmedKey.startsWith("sk_live_")) {
                log.warn("CLERK_SECRET_KEY no tiene el formato esperado (debe empezar con sk_test_ o sk_live_)");
            }
            
            log.info("Enviando request a Clerk: URL={}", apiUrl);
            log.info("Body enviado a Clerk: {}", jsonBody);
            log.debug("Secret key prefix: {}", trimmedKey.length() > 10 ? trimmedKey.substring(0, 10) + "..." : "DEMASIADO CORTA");

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    jsonBody,
                    MediaType.get("application/json; charset=utf-8")
            );

            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + trimmedKey)
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
                    errorResult.put("secretKeyPrefix", secretKey != null && secretKey.length() > 10 
                            ? secretKey.substring(0, 10) + "..." : "NO CONFIGURADA");
                    
                    // Si es 401, agregar información adicional
                    if (response.code() == 401) {
                        errorResult.put("diagnosis", "401 Unauthorized - Verifica que CLERK_SECRET_KEY sea válida y tenga el formato correcto (sk_test_... o sk_live_...)");
                        errorResult.put("tip", "Obtén la Secret Key desde: https://dashboard.clerk.com -> Tu aplicación -> API Keys -> Secret Keys");
                    }
                    
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
     * Endpoint de prueba para verificar el formato del JSON que se envía a Clerk.
     * 
     * Este endpoint te permite ver exactamente qué JSON se construye antes de enviarlo a Clerk.
     * 
     * @param request Datos del usuario
     * @return El JSON que se enviaría a Clerk
     */
    @PostMapping("/test-format")
    public ResponseEntity<Map<String, Object>> testFormat(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Formato 1: email_address directo (mínimo)
        Map<String, Object> format1 = new HashMap<>();
        format1.put("email_address", request.getEmail());
        
        // Formato 2: email_address directo con password
        Map<String, Object> format2 = new HashMap<>();
        format2.put("email_address", request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            format2.put("password", request.getPassword());
            format2.put("skip_password_checks", true);
        }
        
        // Formato 3: email_address directo completo
        Map<String, Object> format3 = new HashMap<>();
        format3.put("email_address", request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            format3.put("password", request.getPassword());
        }
        if (request.getFirstName() != null) {
            format3.put("first_name", request.getFirstName());
        }
        if (request.getLastName() != null) {
            format3.put("last_name", request.getLastName());
        }
        format3.put("skip_password_checks", true);
        
        // Formato 4: email_addresses como array (mínimo)
        Map<String, Object> format4 = new HashMap<>();
        List<Map<String, Object>> emailAddresses1 = new ArrayList<>();
        Map<String, Object> emailObj1 = new HashMap<>();
        emailObj1.put("email_address", request.getEmail());
        emailAddresses1.add(emailObj1);
        format4.put("email_addresses", emailAddresses1);
        
        // Formato 5: email_addresses como array completo
        Map<String, Object> format5 = new HashMap<>();
        List<Map<String, Object>> emailAddresses2 = new ArrayList<>();
        Map<String, Object> emailObj2 = new HashMap<>();
        emailObj2.put("email_address", request.getEmail());
        emailAddresses2.add(emailObj2);
        format5.put("email_addresses", emailAddresses2);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            format5.put("password", request.getPassword());
        }
        if (request.getFirstName() != null) {
            format5.put("first_name", request.getFirstName());
        }
        if (request.getLastName() != null) {
            format5.put("last_name", request.getLastName());
        }
        format5.put("skip_password_checks", true);
        
        try {
            response.put("format1_minimo_solo_email", objectMapper.writeValueAsString(format1));
            response.put("format2_email_con_password", objectMapper.writeValueAsString(format2));
            response.put("format3_email_completo", objectMapper.writeValueAsString(format3));
            response.put("format4_email_addresses_minimo", objectMapper.writeValueAsString(format4));
            response.put("format5_email_addresses_completo", objectMapper.writeValueAsString(format5));
            response.put("note", "Prueba estos formatos en orden. Empieza con format1 (mínimo) y ve agregando campos.");
            response.put("clerk_api_url", clerkProperties.getApiUrl() != null ? clerkProperties.getApiUrl() + "/v1/users" : "https://api.clerk.com/v1/users");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba crear usuario directamente con Clerk usando diferentes formatos.
     * 
     * Este endpoint prueba múltiples formatos hasta encontrar uno que funcione.
     * 
     * @param request Datos del usuario
     * @return Resultado de cada intento
     */
    @PostMapping("/test-create-direct")
    public ResponseEntity<Map<String, Object>> testCreateDirect(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> attempts = new ArrayList<>();
        
        // Validar campos requeridos
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "El campo 'email' es requerido",
                            "receivedRequest", Map.of(
                                    "email", request.getEmail(),
                                    "firstName", request.getFirstName(),
                                    "lastName", request.getLastName()
                            )
                    ));
        }
        
        // Validar que la secret key esté configurada
        String secretKey = clerkProperties.getSecretKey();
        if (secretKey == null || secretKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "CLERK_SECRET_KEY no está configurada",
                            "message", "Verifica que la variable de entorno CLERK_SECRET_KEY esté configurada",
                            "tip", "Ejecuta GET /api/v1/config/clerk para verificar la configuración"
                    ));
        }
        
        // Log de diagnóstico
        String secretKeyPrefix = secretKey.length() > 15 ? secretKey.substring(0, 15) + "..." : secretKey;
        log.info("CLERK_SECRET_KEY detectada: {}", secretKeyPrefix);
        log.info("Longitud de CLERK_SECRET_KEY: {}", secretKey.length());
        log.info("CLERK_SECRET_KEY empieza con 'sk_test_': {}", secretKey.startsWith("sk_test_"));
        log.info("CLERK_SECRET_KEY empieza con 'sk_live_': {}", secretKey.startsWith("sk_live_"));
        log.info("Email recibido: {}", request.getEmail());
        log.info("Request completo: email={}, firstName={}, lastName={}", 
                request.getEmail(), request.getFirstName(), request.getLastName());
        
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
        
        String apiUrl = baseApiUrl + "/v1/users";
        log.info("URL final para Clerk API: {}", apiUrl);
        
        // Agregar información de diagnóstico a la respuesta
        response.put("diagnostic", Map.of(
                "secretKeyLength", secretKey.length(),
                "secretKeyPrefix", secretKeyPrefix,
                "secretKeyStartsWithSkTest", secretKey.startsWith("sk_test_"),
                "secretKeyStartsWithSkLive", secretKey.startsWith("sk_live_"),
                "apiUrl", apiUrl,
                "baseApiUrl", baseApiUrl
        ));
        
        // Formato 1: email_address (singular) como array de strings - FORMATO CORRECTO
        try {
            String email = request.getEmail() != null ? request.getEmail().trim() : null;
            if (email == null || email.isEmpty()) {
                attempts.add(Map.of("format", "email_address array (CORRECTO)", 
                        "error", "Email es null o vacío"));
            } else {
                Map<String, Object> format1 = new HashMap<>();
                List<String> emailAddressList = new ArrayList<>();
                emailAddressList.add(email);
                format1.put("email_address", emailAddressList); // Singular, array de strings
                if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                    format1.put("password", request.getPassword());
                }
                if (request.getFirstName() != null) format1.put("first_name", request.getFirstName());
                if (request.getLastName() != null) format1.put("last_name", request.getLastName());
                format1.put("skip_password_checks", true);
                String json1 = objectMapper.writeValueAsString(format1);
                log.info("Formato 1 - JSON CORRECTO a enviar: {}", json1);
                Map<String, Object> result1 = tryCreateUserRequest(apiUrl, secretKey, json1);
                Map<String, Object> attempt1 = new HashMap<>();
                attempt1.put("format", "email_address array (FORMATO CORRECTO)");
                attempt1.put("json", json1);
                attempt1.put("result", result1);
                attempts.add(attempt1);
            }
        } catch (Exception e) {
            log.error("Error en formato 1", e);
            attempts.add(Map.of("format", "email_address array (CORRECTO)", "error", e.getMessage()));
        }
        
        
        response.put("attempts", attempts);
        response.put("apiUrl", apiUrl);
        response.put("note", "Revisa cuál formato funcionó (httpCode 200 o 201)");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Sincroniza un usuario existente de Clerk a la base de datos local.
     * 
     * @param clerkId ID del usuario en Clerk
     * @return Información del usuario sincronizado
     */
    @PostMapping("/sync-user/{clerkId}")
    public ResponseEntity<Map<String, Object>> syncUserFromClerk(@PathVariable String clerkId) {
        try {
            // Normalizar la URL base
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
            
            String apiUrl = baseApiUrl + "/v1/users/" + clerkId;
            String secretKey = clerkProperties.getSecretKey();

            if (secretKey == null || secretKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "CLERK_SECRET_KEY no está configurada"));
            }

            // Obtener información del usuario de Clerk
            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("Authorization", "Bearer " + secretKey)
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                    return ResponseEntity.status(response.code())
                            .body(Map.of("error", "Error al obtener usuario de Clerk: " + errorBody));
                }

                String responseBody = response.body().string();
                JsonNode userNode = objectMapper.readTree(responseBody);
                
                // Extraer información del usuario
                String email = userNode.has("email_addresses") && userNode.get("email_addresses").isArray() 
                        && userNode.get("email_addresses").size() > 0
                        ? userNode.get("email_addresses").get(0).get("email_address").asText()
                        : null;
                
                String firstName = userNode.has("first_name") ? userNode.get("first_name").asText() : null;
                String lastName = userNode.has("last_name") ? userNode.get("last_name").asText() : null;
                String nombre = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                nombre = nombre.trim();
                if (nombre.isEmpty()) {
                    nombre = email != null ? email.split("@")[0] : "Usuario";
                }
                
                // Crear o actualizar usuario en BD local
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .clerkId(clerkId)
                        .email(email != null ? email : "sin-email@example.com")
                        .nombre(nombre)
                        .rol("cliente") // Por defecto
                        .build();

                UsuarioResponseDTO usuarioCreado = usuarioService.createUsuario(usuarioDTO);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Usuario sincronizado exitosamente");
                result.put("clerkId", clerkId);
                result.put("usuarioId", usuarioCreado.getUsuarioId());
                result.put("email", usuarioCreado.getEmail());
                result.put("nombre", usuarioCreado.getNombre());
                
                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            log.error("Error al sincronizar usuario de Clerk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al sincronizar usuario: " + e.getMessage()));
        }
    }

    /**
     * Obtiene un token JWT de Clerk para un usuario específico.
     * 
     * Este endpoint:
     * 1. Crea una sesión en Clerk
     * 2. Obtiene el token JWT usando la API de Clerk
     * 
     * Requiere que tengas una plantilla JWT configurada en Clerk Dashboard.
     * Por defecto usa "default" como nombre de plantilla.
     * 
     * @param clerkId ID del usuario en Clerk
     * @param template Nombre de la plantilla JWT (opcional, por defecto "default")
     * @return Token JWT y información de la sesión
     */
    @PostMapping("/get-token/{clerkId}")
    public ResponseEntity<Map<String, Object>> getTokenForUser(
            @PathVariable String clerkId,
            @RequestParam(required = false, defaultValue = "default") String template) {
        try {
            // Normalizar la URL base
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
            
            String secretKey = clerkProperties.getSecretKey();
            if (secretKey == null || secretKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "CLERK_SECRET_KEY no está configurada"));
            }

            // 1. Crear sesión en Clerk
            String sessionApiUrl = baseApiUrl + "/v1/sessions";
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("user_id", clerkId);
            
            String sessionJson = objectMapper.writeValueAsString(sessionData);
            okhttp3.RequestBody sessionBody = okhttp3.RequestBody.create(
                    sessionJson,
                    MediaType.get("application/json; charset=utf-8")
            );

            Request sessionRequest = new Request.Builder()
                    .url(sessionApiUrl)
                    .post(sessionBody)
                    .addHeader("Authorization", "Bearer " + secretKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            String sessionId;
            try (Response sessionResponse = httpClient.newCall(sessionRequest).execute()) {
                if (!sessionResponse.isSuccessful()) {
                    String errorBody = sessionResponse.body() != null ? sessionResponse.body().string() : "Sin detalles";
                    return ResponseEntity.status(sessionResponse.code())
                            .body(Map.of(
                                    "error", "Error al crear sesión en Clerk",
                                    "details", errorBody,
                                    "tip", "Verifica que el clerkId sea válido"
                            ));
                }

                String sessionResponseBody = sessionResponse.body().string();
                JsonNode sessionNode = objectMapper.readTree(sessionResponseBody);
                sessionId = sessionNode.get("id").asText();
            }

            // 2. Obtener token JWT de la sesión usando el endpoint de Clerk
            // GET /v1/sessions/{session_id}/tokens/{template_name}
            String tokenApiUrl = baseApiUrl + "/v1/sessions/" + sessionId + "/tokens/" + template;
            
            Request tokenRequest = new Request.Builder()
                    .url(tokenApiUrl)
                    .get()
                    .addHeader("Authorization", "Bearer " + secretKey)
                    .build();

            String jwtToken;
            try (Response tokenResponse = httpClient.newCall(tokenRequest).execute()) {
                if (!tokenResponse.isSuccessful()) {
                    String errorBody = tokenResponse.body() != null ? tokenResponse.body().string() : "Sin detalles";
                    return ResponseEntity.status(tokenResponse.code())
                            .body(Map.of(
                                    "error", "Error al obtener token JWT de Clerk",
                                    "details", errorBody,
                                    "sessionId", sessionId,
                                    "template", template,
                                    "tip", "Verifica que la plantilla JWT '" + template + "' esté configurada en Clerk Dashboard. " +
                                            "Ve a: Dashboard → JWT Templates → Crea o verifica que existe una plantilla con ese nombre."
                            ));
                }

                String tokenResponseBody = tokenResponse.body().string();
                JsonNode tokenNode = objectMapper.readTree(tokenResponseBody);
                jwtToken = tokenNode.has("jwt") ? tokenNode.get("jwt").asText() : null;
                
                if (jwtToken == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "error", "No se pudo obtener el token JWT",
                                    "response", tokenResponseBody,
                                    "tip", "Verifica que la plantilla JWT esté correctamente configurada"
                            ));
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Token JWT obtenido exitosamente");
            result.put("clerkId", clerkId);
            result.put("sessionId", sessionId);
            result.put("template", template);
            result.put("token", jwtToken);
            result.put("usage", "Usa este token en el header: Authorization: Bearer " + jwtToken.substring(0, Math.min(50, jwtToken.length())) + "...");
            result.put("note", "Este token expirará según la configuración de la plantilla JWT. " +
                    "Para producción, los usuarios deben autenticarse desde el frontend para obtener tokens.");
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error al obtener token para usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener token: " + e.getMessage()));
        }
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
     * Asigna eco-puntos directamente a un usuario (solo para pruebas).
     * 
     * Este endpoint permite asignar eco-puntos de forma manual para probar funcionalidades
     * como canjes de recompensas sin necesidad de crear pedidos entregados.
     * 
     * Endpoint: POST /api/v1/test/clerk/assign-eco-puntos
     * 
     * Body:
     * {
     *   "usuarioId": 1,
     *   "puntos": 200
     * }
     * 
     * @param request Request con usuarioId y puntos a asignar
     * @return ResponseEntity con el usuario actualizado y sus eco-puntos
     */
    @PostMapping("/assign-eco-puntos")
    @Transactional
    public ResponseEntity<Map<String, Object>> assignEcoPuntos(@RequestBody AssignEcoPuntosRequest request) {
        try {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.getUsuarioId()));

            // Validar que los puntos sean positivos
            if (request.getPuntos() == null || request.getPuntos() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Los puntos deben ser un número positivo"));
            }

            // Asignar los puntos
            Integer puntosActuales = usuario.getEcoPuntos() != null ? usuario.getEcoPuntos() : 0;
            Integer nuevosPuntos = puntosActuales + request.getPuntos();
            
            usuario.setEcoPuntos(nuevosPuntos);
            usuario.setNivelEcoPuntos(calcularNivelEcoPuntos(nuevosPuntos));
            usuario.setMetaEcoPuntos(calcularMetaSiguienteNivel(nuevosPuntos));
            
            usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Se asignaron %d eco-puntos al usuario %d", request.getPuntos(), request.getUsuarioId()));
            response.put("usuarioId", usuario.getUsuarioId());
            response.put("puntosAnteriores", puntosActuales);
            response.put("puntosAsignados", request.getPuntos());
            response.put("puntosTotales", nuevosPuntos);
            response.put("nivelEcoPuntos", usuario.getNivelEcoPuntos());
            response.put("metaSiguienteNivel", usuario.getMetaEcoPuntos());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al asignar eco-puntos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al asignar eco-puntos: " + e.getMessage()));
        }
    }

    /**
     * Calcula el nivel de eco-puntos basado en la cantidad de puntos.
     */
    private String calcularNivelEcoPuntos(Integer puntos) {
        if (puntos == null || puntos < 0) {
            return "Bronce";
        }
        
        if (puntos >= 501) {
            return "Platino";
        } else if (puntos >= 301) {
            return "Oro";
        } else if (puntos >= 101) {
            return "Plata";
        } else {
            return "Bronce";
        }
    }

    /**
     * Calcula la meta de puntos para el siguiente nivel.
     */
    private Integer calcularMetaSiguienteNivel(Integer puntos) {
        if (puntos == null || puntos < 0) {
            return 101; // Meta para Plata
        }
        
        if (puntos < 101) {
            return 101; // Meta para Plata
        } else if (puntos < 301) {
            return 301; // Meta para Oro
        } else if (puntos < 501) {
            return 501; // Meta para Platino
        } else {
            // Ya está en Platino, la meta es mantener el nivel
            return 501;
        }
    }

    /**
     * DTO para crear usuarios de prueba.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateUserRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("email")
        private String email;
        
        @com.fasterxml.jackson.annotation.JsonProperty("password")
        private String password;
        
        @com.fasterxml.jackson.annotation.JsonProperty("firstName")
        @com.fasterxml.jackson.annotation.JsonAlias({"first_name", "firstName"})
        private String firstName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("lastName")
        @com.fasterxml.jackson.annotation.JsonAlias({"last_name", "lastName"})
        private String lastName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("rol")
        private String rol;
    }

    /**
     * DTO para asignar eco-puntos de prueba.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignEcoPuntosRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("usuarioId")
        private Integer usuarioId;
        
        @com.fasterxml.jackson.annotation.JsonProperty("puntos")
        private Integer puntos;
    }
}

