package com.ecoshop.controller.marca;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Marca.DashboardMarcaResponse;
import com.ecoshop.dto.Marca.MarcaPaginadoResponse;
import com.ecoshop.dto.Marca.MarcaRequestDTO;
import com.ecoshop.dto.Marca.MarcaResponseDTO;
import com.ecoshop.dto.Marca.MetricasAmbientalesMarcaResponse;
import com.ecoshop.dto.Marca.MetricasVentasResponse;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.service.marca.MarcaMetricasService;
import com.ecoshop.service.marca.MarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar marcas.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre marcas.
 * Todos los endpoints están bajo la ruta base "/api/v1/marcas".
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
 * ✅ POST /api/v1/marcas - Crear una nueva marca (201 Created) - Requiere autenticación
 * ✅ GET /api/v1/marcas - Obtener todas las marcas (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/marcas/{id} - Obtener una marca por ID (200 OK) - Requiere autenticación
 * ✅ PUT /api/v1/marcas/{id} - Actualizar una marca existente (200 OK) - Requiere autenticación + permisos
 * ✅ DELETE /api/v1/marcas/{id} - Eliminar una marca (204 No Content) - Requiere autenticación + permisos
 * ✅ GET /api/v1/marcas/{id}/dashboard - Dashboard completo de métricas (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/marcas/{id}/metricas-ventas - Métricas de ventas (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/marcas/{id}/metricas-ambientales - Métricas ambientales (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/marcas/{id}/productos/estadisticas - Estadísticas por producto (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/marcas/buscar?q=... - Buscar marcas por nombre con paginación (200 OK) - Requiere autenticación
 * 
 * Validación de permisos:
 * - Los usuarios solo pueden actualizar/eliminar sus propias marcas (mismo usuarioId)
 * - Los administradores (rol "admin") pueden actualizar/eliminar cualquier marca
 * - El usuarioId se obtiene automáticamente del token JWT (no se envía en el body)
 * - Relación One-to-One: cada usuario solo puede tener una marca
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 */
@RestController // Indica que esta clase es un controlador REST (combina @Controller + @ResponseBody)
@RequestMapping("/api/v1/marcas") // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor // Genera constructor con los campos finales para inyección de dependencias
public class MarcaController {

    // Servicio que contiene la lógica de negocio
    private final MarcaService marcaService;
    private final MarcaMetricasService marcaMetricasService;

    /**
     * Crea una nueva marca.
     * 
     * Endpoint: POST /api/v1/marcas
     * 
     * El usuarioId se obtiene automáticamente del usuario autenticado (token JWT).
     * No es necesario incluir usuarioId en el body.
     * 
     * @param marcaDTO Datos de la marca a crear (validados automáticamente)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con la marca creada y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/marcas
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "nombreOficial": "EcoLife",
     *   "descripcionSostenible": "Marca comprometida con el medio ambiente",
     *   "sitioWeb": "https://www.ecolife.com",
     *   "logoUrl": "https://cdn.example.com/logos/ecolife.png"
     * }
     */
    @PostMapping
    public ResponseEntity<MarcaResponseDTO> createMarca(
            @Valid @RequestBody MarcaRequestDTO marcaDTO,
            Authentication authentication) {
        // Obtener el usuario autenticado del SecurityContext
        Usuario usuario = (Usuario) authentication.getPrincipal();
        // Establecer el usuarioId del usuario autenticado
        marcaDTO.setUsuarioId(usuario.getUsuarioId());
        
        MarcaResponseDTO newMarca = marcaService.createMarca(marcaDTO);
        return new ResponseEntity<>(newMarca, HttpStatus.CREATED);
    }

    /**
     * Obtiene una marca por su ID.
     * 
     * Endpoint: GET /api/v1/marcas/{id}
     * 
     * @param id Identificador de la marca
     * @return ResponseEntity con la marca encontrada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> getMarcaById(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaService.getMarcaById(id));
    }

    /**
     * Obtiene todas las marcas.
     * 
     * Endpoint: GET /api/v1/marcas
     * 
     * @return ResponseEntity con la lista de marcas y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas
     */
    @GetMapping
    public ResponseEntity<List<MarcaResponseDTO>> getAllMarcas() {
        return ResponseEntity.ok(marcaService.getAllMarcas());
    }

    /**
     * Actualiza una marca existente.
     * 
     * Endpoint: PUT /api/v1/marcas/{id}
     * 
     * @param id Identificador de la marca a actualizar
     * @param marcaDTO Nuevos datos de la marca (validados automáticamente)
     * @return ResponseEntity con la marca actualizada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/marcas/1
     * Content-Type: application/json
     * Body: {
     *   "nombreOficial": "EcoLife Actualizado",
     *   "descripcionSostenible": "Nueva descripción",
     *   ...
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> updateMarca(
            @PathVariable Integer id,
            @Valid @RequestBody MarcaRequestDTO marcaDTO,
            Authentication authentication) {
        // Obtener el usuario autenticado del SecurityContext
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede actualizar su propia marca o ser admin
        MarcaResponseDTO marcaExistente = marcaService.getMarcaById(id);
        if (!marcaExistente.getUsuarioId().equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para actualizar esta marca");
        }
        
        // Establecer el usuarioId del usuario autenticado
        marcaDTO.setUsuarioId(usuario.getUsuarioId());
        
        return ResponseEntity.ok(marcaService.updateMarca(id, marcaDTO));
    }

    /**
     * Elimina una marca.
     * 
     * Endpoint: DELETE /api/v1/marcas/{id}
     * 
     * @param id Identificador de la marca a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/marcas/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarca(
            @PathVariable Integer id,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Validar permisos: solo puede eliminar su propia marca o ser admin
        MarcaResponseDTO marcaExistente = marcaService.getMarcaById(id);
        if (!marcaExistente.getUsuarioId().equals(usuario.getUsuarioId()) && !"admin".equalsIgnoreCase(usuario.getRol())) {
            throw new ForbiddenException("No tienes permisos para eliminar esta marca");
        }
        
        marcaService.deleteMarca(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el dashboard completo de métricas de una marca.
     * 
     * Endpoint: GET /api/v1/marcas/{id}/dashboard
     * 
     * Este endpoint proporciona un dashboard completo con:
     * - Métricas de ventas (total de ventas, pedidos, productos vendidos)
     * - Métricas ambientales (huella de carbono, CO₂ ahorrado, agua ahorrada)
     * - Estadísticas por producto
     * 
     * @param id Identificador de la marca
     * @return ResponseEntity con el dashboard completo y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas/1/dashboard
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardMarcaResponse> getDashboardMarca(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaMetricasService.obtenerDashboardMarca(id));
    }

    /**
     * Obtiene las métricas de ventas de una marca.
     * 
     * Endpoint: GET /api/v1/marcas/{id}/metricas-ventas
     * 
     * @param id Identificador de la marca
     * @return ResponseEntity con las métricas de ventas y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas/1/metricas-ventas
     */
    @GetMapping("/{id}/metricas-ventas")
    public ResponseEntity<MetricasVentasResponse> getMetricasVentas(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaMetricasService.obtenerMetricasVentas(id));
    }

    /**
     * Obtiene las métricas ambientales agregadas de una marca.
     * 
     * Endpoint: GET /api/v1/marcas/{id}/metricas-ambientales
     * 
     * @param id Identificador de la marca
     * @return ResponseEntity con las métricas ambientales y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas/1/metricas-ambientales
     */
    @GetMapping("/{id}/metricas-ambientales")
    public ResponseEntity<MetricasAmbientalesMarcaResponse> getMetricasAmbientales(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaMetricasService.obtenerMetricasAmbientales(id));
    }

    /**
     * Obtiene las estadísticas por producto de una marca.
     * 
     * Endpoint: GET /api/v1/marcas/{id}/productos/estadisticas
     * 
     * Este endpoint está incluido en el dashboard, pero se proporciona
     * como endpoint separado para acceso directo a las estadísticas por producto.
     * 
     * @param id Identificador de la marca
     * @return ResponseEntity con el dashboard completo (que incluye estadísticas por producto) y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/marcas/1/productos/estadisticas
     * 
     * Nota: Este endpoint retorna el dashboard completo, que incluye las estadísticas por producto.
     * Para obtener solo las estadísticas, se puede acceder al campo estadisticasProductos del dashboard.
     */
    @GetMapping("/{id}/productos/estadisticas")
    public ResponseEntity<DashboardMarcaResponse> getEstadisticasProductos(@PathVariable Integer id) {
        // Retornamos el dashboard completo que incluye las estadísticas por producto
        return ResponseEntity.ok(marcaMetricasService.obtenerDashboardMarca(id));
    }

    /**
     * Busca marcas por nombre oficial con paginación.
     * 
     * Endpoint: GET /api/v1/marcas/buscar?q={query}&page={page}&size={size}
     * 
     * Este endpoint permite buscar marcas por nombre oficial (búsqueda parcial, case-insensitive).
     * Los resultados están paginados (6 marcas por página por defecto, primera página es 1).
     * 
     * @param query Término de búsqueda (nombre oficial o parte del nombre)
     * @param page Número de página (opcional, basado en 1, por defecto 1)
     * @param size Tamaño de página (opcional, por defecto 6 según requerimiento UX/UI)
     * @return ResponseEntity con marcas paginadas y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET /api/v1/marcas/buscar?q=ecolife
     * GET /api/v1/marcas/buscar?q=green&page=1&size=6
     * GET /api/v1/marcas/buscar?q=eco&page=2&size=6
     */
    @GetMapping("/buscar")
    public ResponseEntity<MarcaPaginadoResponse> buscarMarcasPorNombre(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        MarcaPaginadoResponse marcas = marcaService.buscarMarcasPorNombre(query, page, size);
        return ResponseEntity.ok(marcas);
    }
}
