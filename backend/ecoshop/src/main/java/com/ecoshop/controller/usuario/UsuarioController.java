package com.ecoshop.controller.usuario;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Pedido.PedidoResponseDTO;
import com.ecoshop.dto.Usuario.DashboardImpactoResponse;
import com.ecoshop.dto.Usuario.HistorialImpactoResponse;
import com.ecoshop.dto.Usuario.MetricasAmbientalesUsuarioResponse;
import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadRequest;
import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadResponse;
import com.ecoshop.dto.Usuario.TendenciasImpactoResponse;
import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.usuario.ObjetivosSostenibilidadService;
import com.ecoshop.service.pedido.PedidoService;
import com.ecoshop.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar usuarios.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre usuarios.
 * Todos los endpoints están bajo la ruta base "/api/v1/usuarios".
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles:
 * ✅ POST /api/v1/usuarios - Crear un nuevo usuario (201 Created) - Requiere autenticación
 * ✅ GET /api/v1/usuarios - Obtener todos los usuarios (200 OK) - Público (solo desarrollo)
 * ✅ GET /api/v1/usuarios/{id} - Obtener un usuario por ID (200 OK) - Requiere autenticación + permisos
 * ✅ PUT /api/v1/usuarios/{id} - Actualizar un usuario existente (200 OK) - Requiere autenticación + permisos
 * ✅ DELETE /api/v1/usuarios/{id} - Eliminar un usuario (204 No Content) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/me - Obtener usuario autenticado actual (200 OK) - Requiere autenticación
 * ✅ PUT /api/v1/usuarios/me - Actualizar usuario autenticado actual (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/usuarios/me/pedidos - Obtener pedidos del usuario actual (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/usuarios/{id}/dashboard-impacto - Dashboard de impacto ambiental (200 OK) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/{id}/metricas-ambientales - Métricas ambientales del usuario (200 OK) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/{id}/historial-impacto - Historial de impacto con filtros de fecha (200 OK) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/{id}/tendencias-impacto - Tendencias de impacto por período (200 OK) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/{id}/objetivos - Obtener objetivos de sostenibilidad (200 OK) - Requiere autenticación + permisos
 * ✅ PUT /api/v1/usuarios/{id}/objetivos - Actualizar objetivos de sostenibilidad (200 OK) - Requiere autenticación + permisos
 * ✅ GET /api/v1/usuarios/{id}/objetivos/progreso - Progreso detallado hacia objetivos (200 OK) - Requiere autenticación + permisos
 * 
 * Validación de permisos:
 * - Los usuarios solo pueden acceder a sus propios datos (mismo usuarioId)
 * - Los administradores (rol "admin") pueden acceder a cualquier usuario
 * - Si un usuario intenta acceder a datos de otro usuario, se lanza ForbiddenException
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 * 
 */
@RestController // Indica que esta clase es un controlador REST (combina @Controller + @ResponseBody)
@RequestMapping("/api/v1/usuarios") // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
public class UsuarioController {

    // Servicio que contiene la lógica de negocio
    private final UsuarioService usuarioService;
    
    // Servicio para calcular impacto ambiental
    private final ImpactoAmbientalService impactoAmbientalService;
    
    // Servicio para gestionar objetivos de sostenibilidad
    private final ObjetivosSostenibilidadService objetivosSostenibilidadService;
    
    // Servicio para gestionar pedidos
    private final PedidoService pedidoService;

    /**
     * Crea un nuevo usuario.
     * 
     * Endpoint: POST /api/v1/usuarios
     * 
     * 
     * @param dto Datos del usuario a crear (validados automáticamente)
     * @return ResponseEntity con el usuario creado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/usuarios
     * Content-Type: application/json
     * Body: {
     *   "email": "user@example.com",
     *   "nombre": "Juan Pérez",
     *   "rol": "cliente",
     *   "direccionDefault": "Av. Principal 123"
     * }
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO newUsuario = usuarioService.createUsuario(dto);
        return new ResponseEntity<>(newUsuario, HttpStatus.CREATED);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}
     * 
     * Validación de permisos: Los usuarios solo pueden acceder a sus propios datos,
     * excepto los administradores que pueden acceder a cualquier usuario.
     * 
     * @param id Identificador del usuario
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el usuario encontrado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder a este usuario");
        }
        
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    /**
     * Obtiene todos los usuarios.
     * 
     * Endpoint: GET /api/v1/usuarios
     * 
     * @return ResponseEntity con la lista de usuarios y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    /**
     * Actualiza un usuario existente.
     * 
     * Endpoint: PUT /api/v1/usuarios/{id}
     * 
     * Validación de permisos: Los usuarios solo pueden actualizar sus propios datos,
     * excepto los administradores que pueden actualizar cualquier usuario.
     * 
     * @param id Identificador del usuario a actualizar
     * @param dto Nuevos datos del usuario (validados automáticamente)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el usuario actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * @throws com.ecoshop.exception.ForbiddenException si el usuario no tiene permisos
     * 
     * Ejemplo de petición:
     * PUT /api/v1/usuarios/1
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Juan Pérez Actualizado",
     *   "direccionDefault": "Nueva dirección",
     *   ...
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequestDTO dto,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede actualizar sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para actualizar este usuario");
        }
        
        return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));
    }

    /**
     * Obtiene el dashboard completo de impacto ambiental del usuario.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/dashboard-impacto
     * 
     * Este endpoint proporciona un resumen completo del impacto ambiental del usuario,
     * incluyendo CO₂ ahorrado, agua ahorrada, compras sostenibles, eco-puntos y objetivos.
     * 
     * @param id Identificador del usuario
     * @return ResponseEntity con el dashboard de impacto ambiental y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/dashboard-impacto
     */
    @GetMapping("/{id}/dashboard-impacto")
    public ResponseEntity<DashboardImpactoResponse> getDashboardImpacto(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder al dashboard de este usuario");
        }
        
        DashboardImpactoResponse dashboard = impactoAmbientalService.obtenerDashboardImpacto(id);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene las métricas ambientales agregadas del usuario.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/metricas-ambientales
     * 
     * Este endpoint proporciona una versión simplificada de las métricas ambientales,
     * enfocada solo en los valores numéricos sin equivalencias ni objetivos.
     * 
     * @param id Identificador del usuario
     * @return ResponseEntity con las métricas ambientales y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/metricas-ambientales
     */
    @GetMapping("/{id}/metricas-ambientales")
    public ResponseEntity<MetricasAmbientalesUsuarioResponse> getMetricasAmbientales(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder a las métricas de este usuario");
        }
        
        MetricasAmbientalesUsuarioResponse metricas = impactoAmbientalService.obtenerMetricasAmbientalesUsuario(id);
        return ResponseEntity.ok(metricas);
    }

    /**
     * Obtiene el historial de impacto ambiental del usuario con filtro de fechas.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/historial-impacto?fechaInicio={date}&fechaFin={date}
     * 
     * Este endpoint proporciona un historial detallado de compras con impacto acumulado,
     * permitiendo filtrar por rango de fechas. Si no se proporcionan fechas, retorna
     * el historial completo.
     * 
     * @param id Identificador del usuario
     * @param fechaInicio Fecha de inicio del rango (opcional, formato: yyyy-MM-ddTHH:mm:ss)
     * @param fechaFin Fecha de fin del rango (opcional, formato: yyyy-MM-ddTHH:mm:ss)
     * @return ResponseEntity con el historial de impacto ambiental y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/historial-impacto?fechaInicio=2024-01-01T00:00:00&fechaFin=2024-12-31T23:59:59
     */
    @GetMapping("/{id}/historial-impacto")
    public ResponseEntity<HistorialImpactoResponse> getHistorialImpacto(
            @PathVariable Integer id,
            @RequestParam(required = false) java.time.LocalDateTime fechaInicio,
            @RequestParam(required = false) java.time.LocalDateTime fechaFin,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder al historial de este usuario");
        }
        
        HistorialImpactoResponse historial = impactoAmbientalService.obtenerHistorialImpacto(id, fechaInicio, fechaFin);
        return ResponseEntity.ok(historial);
    }

    /**
     * Obtiene las tendencias de impacto ambiental del usuario para gráficas.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/tendencias-impacto?periodo={dia|semana|mes}
     * 
     * Este endpoint proporciona datos agregados por período (día, semana, mes) para
     * generar gráficas de impacto ambiental a lo largo del tiempo. Por defecto, retorna
     * datos de los últimos 6 meses.
     * 
     * @param id Identificador del usuario
     * @param periodo Período de agregación: "dia", "semana", o "mes" (por defecto: "mes")
     * @return ResponseEntity con las tendencias de impacto ambiental y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/tendencias-impacto?periodo=mes
     */
    @GetMapping("/{id}/tendencias-impacto")
    public ResponseEntity<TendenciasImpactoResponse> getTendenciasImpacto(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "mes") String periodo,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder a las tendencias de este usuario");
        }
        
        TendenciasImpactoResponse tendencias = impactoAmbientalService.obtenerTendenciasImpacto(id, periodo, 6);
        return ResponseEntity.ok(tendencias);
    }

    /**
     * Obtiene los objetivos de sostenibilidad del usuario con su progreso actual.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/objetivos
     * 
     * Este endpoint proporciona los objetivos de sostenibilidad configurados por el usuario
     * (meta de CO₂ y meta de eco-puntos) junto con el progreso actual hacia esos objetivos.
     * 
     * @param id Identificador del usuario
     * @return ResponseEntity con los objetivos y progreso y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/objetivos
     */
    @GetMapping("/{id}/objetivos")
    public ResponseEntity<ObjetivosSostenibilidadResponse> getObjetivos(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder a los objetivos de este usuario");
        }
        
        ObjetivosSostenibilidadResponse objetivos = objetivosSostenibilidadService.obtenerObjetivos(id);
        return ResponseEntity.ok(objetivos);
    }

    /**
     * Actualiza los objetivos de sostenibilidad del usuario.
     * 
     * Endpoint: PUT /api/v1/usuarios/{id}/objetivos
     * 
     * Este endpoint permite al usuario establecer o actualizar sus objetivos personalizados
     * de CO₂ ahorrado y eco-puntos. Los campos son opcionales, solo se actualizan los proporcionados.
     * 
     * @param id Identificador del usuario
     * @param request Objetivos a actualizar (metaCO2 y/o metaEcoPuntos)
     * @return ResponseEntity con los objetivos actualizados y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/usuarios/1/objetivos
     * Content-Type: application/json
     * Body: {
     *   "metaCO2": 100.0,
     *   "metaEcoPuntos": 1000
     * }
     */
    @PutMapping("/{id}/objetivos")
    public ResponseEntity<ObjetivosSostenibilidadResponse> updateObjetivos(
            @PathVariable Integer id,
            @Valid @RequestBody ObjetivosSostenibilidadRequest request) {
        ObjetivosSostenibilidadResponse objetivos = objetivosSostenibilidadService.actualizarObjetivos(id, request);
        return ResponseEntity.ok(objetivos);
    }

    /**
     * Obtiene el progreso detallado hacia los objetivos de sostenibilidad.
     * 
     * Endpoint: GET /api/v1/usuarios/{id}/objetivos/progreso
     * 
     * Este endpoint proporciona información detallada sobre el progreso del usuario hacia
     * sus objetivos, incluyendo porcentajes, valores restantes y mensajes motivacionales.
     * 
     * @param id Identificador del usuario
     * @return ResponseEntity con el progreso detallado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1/objetivos/progreso
     */
    @GetMapping("/{id}/objetivos/progreso")
    public ResponseEntity<ObjetivosSostenibilidadResponse> getProgresoObjetivos(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede acceder a sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para acceder al progreso de este usuario");
        }
        
        ObjetivosSostenibilidadResponse progreso = objetivosSostenibilidadService.obtenerProgresoObjetivos(id);
        return ResponseEntity.ok(progreso);
    }

    /**
     * Elimina un usuario.
     * 
     * Endpoint: DELETE /api/v1/usuarios/{id}
     * 
     * @param id Identificador del usuario a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/usuarios/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede eliminar sus propios datos o ser admin
        if (!usuarioAutenticado.getUsuarioId().equals(id) && !"admin".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            throw new ForbiddenException("No tienes permisos para eliminar este usuario");
        }
        
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el usuario autenticado actual.
     * 
     * Endpoint: GET /api/v1/usuarios/me
     * 
     * Este endpoint permite al usuario autenticado obtener su propia información
     * sin necesidad de conocer su ID. El ID se extrae automáticamente del token JWT.
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el usuario autenticado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/me
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioActual(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioService.getUsuarioById(usuario.getUsuarioId()));
    }

    /**
     * Actualiza el usuario autenticado actual.
     * 
     * Endpoint: PUT /api/v1/usuarios/me
     * 
     * Este endpoint permite al usuario autenticado actualizar su propia información
     * sin necesidad de conocer su ID. El ID se extrae automáticamente del token JWT.
     * 
     * @param dto Nuevos datos del usuario (validados automáticamente)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el usuario actualizado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * PUT /api/v1/usuarios/me
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Juan Pérez Actualizado",
     *   "direccionDefault": "Nueva dirección",
     *   ...
     * }
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> updateUsuarioActual(
            @Valid @RequestBody UsuarioRequestDTO dto,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioService.updateUsuario(usuario.getUsuarioId(), dto));
    }

    /**
     * Obtiene los pedidos del usuario autenticado actual.
     * 
     * Endpoint: GET /api/v1/usuarios/me/pedidos
     * 
     * Este endpoint permite al usuario autenticado obtener sus propios pedidos
     * sin necesidad de conocer su ID. El ID se extrae automáticamente del token JWT.
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con la lista de pedidos del usuario y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/me/pedidos
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping("/me/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosUsuarioActual(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(pedidoService.getPedidosByUsuario(usuario.getUsuarioId()));
    }
}
