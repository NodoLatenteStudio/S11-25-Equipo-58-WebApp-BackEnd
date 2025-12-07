package com.ecoshop.controller.clerk;

import com.ecoshop.config.ClerkProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar la configuración de Clerk.
 * 
 * Este controlador expone un endpoint que permite verificar que las propiedades
 * de Clerk se hayan cargado correctamente desde las variables de entorno.
 * 
 * IMPORTANTE: Este endpoint solo muestra si las propiedades están configuradas,
 * NO muestra los valores completos de las claves secretas por seguridad.
 * 
 * Endpoints:
 * - GET /api/v1/config/clerk: Verifica la configuración de Clerk
 */
@RestController
@RequestMapping("/api/v1/config/clerk")
@RequiredArgsConstructor
@Slf4j
public class ClerkConfigController {

    private final ClerkProperties clerkProperties;

    /**
     * Verifica la configuración de Clerk.
     * 
     * Este endpoint devuelve información sobre si las propiedades de Clerk
     * están configuradas correctamente, sin exponer los valores completos
     * de las claves secretas.
     * 
     * @return ResponseEntity con información sobre la configuración de Clerk
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Verificar variables de entorno directamente del sistema
        String envSecretKey = System.getenv("CLERK_SECRET_KEY");
        String envPublishableKey = System.getenv("CLERK_PUBLISHABLE_KEY");
        String envIssuer = System.getenv("CLERK_ISSUER");
        String envWebhookSecret = System.getenv("CLERK_WEBHOOK_SECRET");
        String envApiUrl = System.getenv("CLERK_API_URL");
        
        // Verificar si las propiedades están configuradas en ClerkProperties
        boolean secretKeyConfigured = clerkProperties.getSecretKey() != null 
                && !clerkProperties.getSecretKey().isEmpty();
        boolean publishableKeyConfigured = clerkProperties.getPublishableKey() != null 
                && !clerkProperties.getPublishableKey().isEmpty();
        boolean issuerConfigured = clerkProperties.getIssuer() != null 
                && !clerkProperties.getIssuer().isEmpty();
        boolean webhookSecretConfigured = clerkProperties.getWebhookSecret() != null 
                && !clerkProperties.getWebhookSecret().isEmpty();
        boolean apiUrlConfigured = clerkProperties.getApiUrl() != null 
                && !clerkProperties.getApiUrl().isEmpty();
        
        // Información de diagnóstico: variables de entorno del sistema
        config.put("envVar_CLERK_SECRET_KEY", envSecretKey != null ? envSecretKey.substring(0, Math.min(10, envSecretKey.length())) + "..." : "NO ENCONTRADA");
        config.put("envVar_CLERK_PUBLISHABLE_KEY", envPublishableKey != null ? envPublishableKey.substring(0, Math.min(15, envPublishableKey.length())) + "..." : "NO ENCONTRADA");
        config.put("envVar_CLERK_ISSUER", envIssuer != null ? envIssuer : "NO ENCONTRADA");
        config.put("envVar_CLERK_WEBHOOK_SECRET", envWebhookSecret != null ? envWebhookSecret.substring(0, Math.min(10, envWebhookSecret.length())) + "..." : "NO ENCONTRADA");
        config.put("envVar_CLERK_API_URL", envApiUrl != null ? envApiUrl : "NO ENCONTRADA");

        // Información general (sin exponer valores completos)
        config.put("secretKeyConfigured", secretKeyConfigured);
        config.put("secretKeyPrefix", secretKeyConfigured 
                ? clerkProperties.getSecretKey().substring(0, Math.min(10, clerkProperties.getSecretKey().length())) + "..." 
                : "NO CONFIGURADO");
        
        config.put("publishableKeyConfigured", publishableKeyConfigured);
        config.put("publishableKeyPrefix", publishableKeyConfigured 
                ? clerkProperties.getPublishableKey().substring(0, Math.min(15, clerkProperties.getPublishableKey().length())) + "..." 
                : "NO CONFIGURADO");
        
        config.put("issuerConfigured", issuerConfigured);
        config.put("issuer", issuerConfigured ? clerkProperties.getIssuer() : "NO CONFIGURADO");
        
        config.put("webhookSecretConfigured", webhookSecretConfigured);
        config.put("webhookSecretPrefix", webhookSecretConfigured 
                ? clerkProperties.getWebhookSecret().substring(0, Math.min(10, clerkProperties.getWebhookSecret().length())) + "..." 
                : "NO CONFIGURADO");
        
        config.put("apiUrlConfigured", apiUrlConfigured);
        config.put("apiUrl", apiUrlConfigured ? clerkProperties.getApiUrl() : "NO CONFIGURADO");

        // Estado general
        boolean allConfigured = secretKeyConfigured && publishableKeyConfigured 
                && issuerConfigured && apiUrlConfigured;
        config.put("allRequiredConfigured", allConfigured);
        config.put("status", allConfigured ? "OK" : "INCOMPLETO");
        
        // Mensaje de diagnóstico mejorado
        StringBuilder message = new StringBuilder();
        if (allConfigured) {
            message.append("Configuración de Clerk completa");
        } else {
            message.append("Faltan algunas propiedades de Clerk. ");
            if (!publishableKeyConfigured) {
                message.append("CLERK_PUBLISHABLE_KEY no está configurada. ");
                if (envPublishableKey == null || envPublishableKey.isEmpty()) {
                    message.append("La variable de entorno no se está cargando. Verifica que el archivo .env tenga la línea: CLERK_PUBLISHABLE_KEY=pk_test_...");
                } else {
                    message.append("La variable de entorno existe pero no se está mapeando correctamente a ClerkProperties.");
                }
            }
        }
        config.put("message", message.toString());

        log.info("Verificación de configuración de Clerk: {}", config.get("status"));

        return ResponseEntity.ok(config);
    }
}

