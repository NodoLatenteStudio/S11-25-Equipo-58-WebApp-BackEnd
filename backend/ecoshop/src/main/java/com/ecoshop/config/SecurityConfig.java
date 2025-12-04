package com.ecoshop.config;

import com.ecoshop.repository.UsuarioRepository;
import com.ecoshop.security.clerk.ClerkJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad de Spring Security.
 * 
 * ======================================================================================
 * CONFIGURACIÓN CON OAUTH2 RESOURCE SERVER:
 * ======================================================================================
 * 
 * Spring Security OAuth2 Resource Server maneja automáticamente:
 * - Validación de firma JWT usando JWKS (cache automático)
 * - Verificación de expiración y claims estándar
 * - Integración con el issuer configurado
 * - Manejo de errores estándar
 * 
 * Nuestro convertidor personalizado solo se encarga de:
 * - Mapear JWT.sub → clerkId → Usuario de BD
 * - Crear Authentication con el Usuario como principal
 * 
 * FLUJO:
 * ------------
 * 1. Cliente envía: Authorization: Bearer <token>
 * 2. Spring Security OAuth2 Resource Server intercepta automáticamente
 * 3. Spring valida el token JWT (firma, expiración, issuer) usando JWKS de Clerk
 * 4. ClerkJwtAuthenticationConverter convierte JWT → Authentication:
 *    a. Extrae clerkId de jwt.getSubject()
 *    b. Busca Usuario en BD por clerkId
 *    c. Crea Authentication con Usuario y ROLE_<rol>
 * 5. SecurityContext contiene el Usuario autenticado
 * 
 * ======================================================================================
 * 
 * Configuración actual:
 * - CSRF deshabilitado: Para APIs REST no es necesario (usamos tokens JWT de Clerk)
 * - CORS habilitado: Permite solicitudes desde diferentes orígenes
 * - Sesiones stateless: No se mantiene estado de sesión (típico para APIs REST)
 * - OAuth2 Resource Server: Usa JWT de Clerk para autenticación
 * - Endpoints públicos: /api/v1/health, /api/v1/webhooks/clerk, /api/v1/config/clerk, 
 *   /api/v1/test/clerk/**, /api/v1/certificaciones/**, /api/v1/productos/**
 * - Endpoints protegidos: /api/v1/usuarios/**, /api/v1/marcas/**, /api/v1/pedidos/**, 
 *   /api/v1/pedido-items/**
 * 
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    /**
     * Configura la cadena de filtros de seguridad con OAuth2 Resource Server.
     * 
     * Este método define cómo Spring Security maneja las solicitudes HTTP.
     * 
     * Configuraciones aplicadas:
     * 1. CSRF deshabilitado: Para APIs REST no se necesita protección CSRF
     * 
     * 2. CORS habilitado: Permite solicitudes desde diferentes orígenes
     *    (configurado en el método corsConfigurationSource())
     * 
     * 3. Sesiones stateless: No se mantiene estado de sesión entre solicitudes
     *    (típico para APIs REST, cada solicitud es independiente)
     * 
     * 4. OAuth2 Resource Server con JWT:
     *    - Spring valida automáticamente los tokens JWT usando JWKS de Clerk
     *    - Nuestro convertidor personalizado mapea el JWT → Usuario de BD
     *    - La configuración del issuer se toma de application.yml:
     *      spring.security.oauth2.resourceserver.jwt.issuer-uri
     * 
     * 5. Autorización de endpoints:
     *    - Endpoints públicos: No requieren autenticación
     *    - Endpoints protegidos: Requieren token JWT válido de Clerk
     * 
     * @param http Objeto HttpSecurity para configurar la seguridad
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay un error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF (Cross-Site Request Forgery)
                // Para APIs REST no es necesario, ya que no usamos cookies de sesión
                .csrf(AbstractHttpConfigurer::disable)
                
                // Habilita CORS y configura la fuente de configuración
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configura la gestión de sesiones como stateless
                // No se mantiene estado de sesión entre solicitudes (típico para APIs REST)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configura OAuth2 Resource Server con JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Usa nuestro convertidor personalizado para mapear JWT → Usuario
                                .jwtAuthenticationConverter(new ClerkJwtAuthenticationConverter(usuarioRepository))
                        )
                )
                
                // Configura la autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Estos endpoints son públicos (no requieren autenticación)
                        .requestMatchers(
                          "/api/v1/health",
                          "/api/v1/webhooks/clerk", // Webhooks de Clerk (deben verificar firma)
                          "/api/v1/config/clerk", // Verificación de configuración de Clerk
                          "/api/v1/test/clerk/**", // Endpoints de prueba de Clerk (SOLO desarrollo)
                          "/api/v1/certificaciones/**", // Lectura pública de certificaciones
                          "/api/v1/productos/**" // Lectura pública de productos
                          ).permitAll()
                        // Endpoints que requieren autenticación
                        .requestMatchers(
                          "/api/v1/usuarios/**", // Gestión de usuarios (requiere autenticación)
                          "/api/v1/marcas/**", // Gestión de marcas (requiere autenticación)
                          "/api/v1/pedidos/**", // Gestión de pedidos (requiere autenticación)
                          "/api/v1/pedido-items/**" // Gestión de items de pedido (requiere autenticación)
                        ).authenticated()
                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Configura la fuente de configuración de CORS.
     * 
     * CORS (Cross-Origin Resource Sharing) permite que el navegador permita
     * solicitudes desde diferentes orígenes (dominios, puertos, protocolos).
     * 
     * Configuración actual:
     * - Orígenes permitidos: Todos (*) - Cambiar en producción
     * - Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS
     * - Headers permitidos: Todos (*)
     * - Headers expuestos: Authorization, Content-Type
     * - Credenciales: No permitidas (allowCredentials = false)
     * - Tiempo de caché: 3600 segundos (1 hora)
     * 
     * IMPORTANTE para producción:
     * - Especificar dominios específicos en lugar de "*"
     * - Ejemplo: configuration.setAllowedOrigins(Arrays.asList("https://frontend.com", "https://www.frontend.com"))
     * - Revisar y ajustar según las necesidades de seguridad
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // En producción, especificar dominios específicos en lugar de "*"
        // Ejemplo: configuration.setAllowedOrigins(Arrays.asList("https://frontend.com"))
        configuration.setAllowedOrigins(List.of("*")); // Permite todos los orígenes
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Headers permitidos en las solicitudes
        configuration.setAllowedHeaders(List.of("*")); // Permite todos los headers
        
        // Headers que el cliente puede leer en la respuesta
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // No permite credenciales (cookies, autenticación HTTP) en las solicitudes
        // Si necesitas enviar credenciales, cambiar a true y especificar orígenes específicos
        configuration.setAllowCredentials(false);
        
        // Tiempo que el navegador puede cachear la respuesta de la preflight (OPTIONS)
        // 3600 segundos = 1 hora
        configuration.setMaxAge(3600L);

        // Registra la configuración de CORS para todas las rutas ("/**")
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}