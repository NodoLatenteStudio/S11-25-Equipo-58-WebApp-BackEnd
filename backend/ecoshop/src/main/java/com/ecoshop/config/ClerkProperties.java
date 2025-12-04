package com.ecoshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para Clerk.
 * 
 * Esta clase carga las propiedades de configuración de Clerk desde application.yml.
 * Las propiedades se prefijan con "clerk" en el archivo de configuración.
 * 
 */
@Configuration
@ConfigurationProperties(prefix = "clerk")
@Data
public class ClerkProperties {

    /**
     * Clave secreta de Clerk (Secret Key).
     * 
     * Se usa para:
     * - Validar tokens JWT de Clerk
     * - Autenticar webhooks de Clerk
     * - Realizar operaciones administrativas con la API de Clerk
     * 
     * Formato: sk_test_xxxxx o sk_live_xxxxx
     */
    private String secretKey;

    /**
     * Clave pública de Clerk (Publishable Key).
     * 
     * Se usa principalmente en el frontend para inicializar Clerk.
     * Puede ser útil en el backend para algunas operaciones.
     * 
     * Formato: pk_test_xxxxx o pk_live_xxxxx
     */
    private String publishableKey;

    /**
     * Secreto de webhook de Clerk.
     * 
     * Se usa para verificar que los webhooks realmente provienen de Clerk.
     * Es crítico para la seguridad de los webhooks.
     * 
     * Formato: whsec_xxxxx
     */
    private String webhookSecret;

    /**
     * Issuer (emisor) de los tokens JWT de Clerk.
     * 
     * URL base de tu instancia de Clerk.
     * Se usa para validar que los tokens JWT fueron emitidos por Clerk.
     * 
     * Ejemplo: https://your-clerk-instance.clerk.accounts.dev
     */
    private String issuer;

    /**
     * URL de la API de Clerk para obtener información de usuarios.
     * 
     * Por defecto: https://api.clerk.com
     * Puede cambiar según tu región o configuración.
     * 
     * NOTA: Según la documentación de Clerk, la URL base es https://api.clerk.com
     * Los endpoints se agregan directamente (ej: /users, /sessions, etc.)
     */
    private String apiUrl = "https://api.clerk.com";
}

