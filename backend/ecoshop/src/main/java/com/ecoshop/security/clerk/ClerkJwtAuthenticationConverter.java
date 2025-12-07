package com.ecoshop.security.clerk;

import com.ecoshop.domain.Usuario;
import com.ecoshop.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.Optional;

/**
 * Convertidor personalizado de JWT a Authentication para Clerk.
 * 
 * Este convertidor se ejecuta después de que Spring Security valida el token JWT.
 * Su responsabilidad es:
 * 1. Extraer el clerkId del claim "sub" del token JWT
 * 2. Buscar el Usuario en la base de datos usando el clerkId
 * 3. Crear un Authentication con:
 *    - principal = Usuario (objeto completo)
 *    - authorities = ROLE_<ROL_EN_MAYUSCULAS>
 * 
 * IMPORTANTE:
 * - El token JWT ya fue validado por Spring Security (firma, expiración, issuer)
 * - El clerkId en el token debe existir en la BD (sincronizado vía webhook)
 * - Si el usuario no existe en la BD, se lanza excepción (autenticación falla)
 * 
 * FLUJO:
 * 1. Cliente envía: Authorization: Bearer <token>
 * 2. Spring valida el token (firma, expiración, issuer) usando JWKS de Clerk
 * 3. Este convertidor mapea el token validado → Usuario de la BD → Authentication
 * 4. SecurityContext contiene el Usuario autenticado
 */
@Slf4j
@RequiredArgsConstructor
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final UsuarioRepository usuarioRepository;

    /**
     * Convierte un JWT validado en un Authentication con el Usuario de la BD.
     * 
     * @param jwt Token JWT ya validado por Spring Security
     * @return Authentication con el Usuario como principal y su rol como authority
     * @throws BadCredentialsException si el usuario no existe en la BD o falta clerkId
     */
    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        // 1. Extraer clerkId del claim "sub" (subject)
        String clerkId = jwt.getSubject();
        
        if (clerkId == null || clerkId.isEmpty()) {
            log.error("Token JWT no contiene claim 'sub' válido (clerkId)");
            throw new BadCredentialsException("Token JWT inválido: falta el claim 'sub' (clerkId)");
        }

        log.debug("Convirtiendo JWT a Authentication para clerkId: {}", clerkId);

        // 2. Buscar el Usuario en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByClerkId(clerkId);

        if (usuarioOpt.isEmpty()) {
            log.warn(
                "Token JWT válido pero usuario no encontrado en BD para clerkId: {}. " +
                "Esto indica que el usuario no está sincronizado. " +
                "Posibles causas: " +
                "1) El webhook 'user.created' no se procesó correctamente, " +
                "2) El usuario fue eliminado de la BD, " +
                "3) Hay una inconsistencia entre Clerk y la BD local.",
                clerkId
            );
            throw new BadCredentialsException(
                String.format("Usuario no encontrado en la base de datos para clerkId: %s. " +
                    "Verifica que el usuario esté sincronizado desde Clerk.", clerkId)
            );
        }

        Usuario usuario = usuarioOpt.get();

        // 3. Crear authorities basadas en el rol del usuario
        // Formato: ROLE_<ROL_EN_MAYUSCULAS> (ej: ROLE_CLIENTE, ROLE_MARCA, ROLE_ADMIN)
        String authority = "ROLE_" + usuario.getRol().toUpperCase();
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);

        log.debug("Usuario autenticado: {} (clerkId: {}, rol: {})", 
            usuario.getEmail(), clerkId, usuario.getRol());

        // 4. Crear y retornar Authentication
        // - principal = Usuario (objeto completo disponible en controllers)
        // - credentials = null (no se necesitan)
        // - authorities = [ROLE_<ROL>]
        return new UsernamePasswordAuthenticationToken(
            usuario,
            null, // credentials
            Collections.singletonList(grantedAuthority)
        );
    }
}
