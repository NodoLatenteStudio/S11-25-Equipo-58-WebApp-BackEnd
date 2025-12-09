package com.ecoshop.controller.certificacion;

import com.ecoshop.dto.Certificacion.CertificacionRequestDTO;
import com.ecoshop.dto.Certificacion.CertificacionResponseDTO;
import com.ecoshop.service.certificacion.CertificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar certificaciones.
 * 
 * Este controlador expone los endpoints CRUD para la gestión de certificaciones.
 * Todos los endpoints están bajo la ruta base "/api/v1/certificaciones".
 * 
 * Flujo de datos:
 * 1. El cliente envía una petición HTTP
 * 2. El controlador valida la petición (si aplica)
 * 3. El controlador delega la lógica de negocio al servicio
 * 4. El servicio procesa la petición y retorna un DTO
 * 5. El controlador envía la respuesta HTTP al cliente
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles (todos públicos):
 * ✅ POST /api/v1/certificaciones - Crear una nueva certificación
 * ✅ GET /api/v1/certificaciones - Obtener todas las certificaciones
 * ✅ GET /api/v1/certificaciones/{id} - Obtener una certificación por ID
 * ✅ GET /api/v1/certificaciones/sello/{nombreSello} - Obtener una certificación por nombre de sello
 * ✅ PUT /api/v1/certificaciones/{id} - Actualizar una certificación
 * ✅ DELETE /api/v1/certificaciones/{id} - Eliminar una certificación
 * 
 * Nota: Todos los endpoints de certificaciones son públicos (no requieren autenticación)
 * según la configuración en SecurityConfig.
 * 
 * Validaciones:
 * - Las validaciones de datos se realizan automáticamente mediante @Valid
 * - Si la validación falla, se lanza MethodArgumentNotValidException
 * - El GlobalExceptionHandler captura y formatea los errores de validación
 */
@RestController
@RequestMapping("/api/v1/certificaciones")
@RequiredArgsConstructor // Genera automáticamente un constructor con los campos finales (inyección de dependencias)
public class CertificacionController {

    // Inyección de dependencias: Spring proporciona automáticamente una instancia de CertificacionService
    private final CertificacionService certificacionService;

    /**
     * Crea una nueva certificación en la base de datos.
     * 
     * @param dto Datos de la certificación a crear (viene en el cuerpo de la petición)
     * @return ResponseEntity con la certificación creada y código HTTP 201 (CREATED)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException si los datos no son válidos
     * @throws com.ecoshop.exception.BadRequestException si ya existe una certificación con el mismo nombre de sello
     * 
     * Validaciones aplicadas (definidas en CertificacionRequestDTO):
     * - nombreSello: obligatorio
     * - descripcion: opcional
     * - entidadEmisora: opcional
     * 
     * Ejemplo de uso:
     * POST http://localhost:8080/api/v1/certificaciones
     * Body: {
     *   "nombreSello": "Fair Trade",
     *   "descripcion": "Certificación que garantiza condiciones de comercio justo",
     *   "entidadEmisora": "Fair Trade International"
     * }
     */
    @PostMapping
    public ResponseEntity<CertificacionResponseDTO> createCertificacion(@Valid @RequestBody CertificacionRequestDTO dto) {
        // @Valid activa las validaciones definidas en CertificacionRequestDTO
        // @RequestBody convierte el JSON del cuerpo de la petición a un objeto CertificacionRequestDTO
        CertificacionResponseDTO createdCertificacion = certificacionService.createCertificacion(dto);
        // Retornamos código HTTP 201 (CREATED) para indicar que se creó un nuevo recurso
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCertificacion);
    }

    /**
     * Obtiene una certificación específica por su ID.
     * 
     * @param id Identificador único de la certificación
     * @return ResponseEntity con la certificación encontrada (CertificacionResponseDTO) y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/certificaciones/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CertificacionResponseDTO> getCertificacionById(@PathVariable Integer id) {
        // @PathVariable extrae el valor del ID desde la URL
        CertificacionResponseDTO certificacion = certificacionService.getCertificacionById(id);
        return ResponseEntity.ok(certificacion);
    }

    /**
     * Obtiene todas las certificaciones existentes en la base de datos.
     * 
     * @return ResponseEntity con la lista de certificaciones (CertificacionResponseDTO) y código HTTP 200 (OK)
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/certificaciones
     */
    @GetMapping
    public ResponseEntity<List<CertificacionResponseDTO>> getAllCertificaciones() {
        // Delegamos la lógica al servicio para mantener separación de responsabilidades
        List<CertificacionResponseDTO> certificaciones = certificacionService.getAllCertificaciones();
        return ResponseEntity.ok(certificaciones); // Retorna código HTTP 200 con la lista de certificaciones
    }

    /**
     * Obtiene una certificación específica por su nombre de sello.
     * 
     * Este endpoint es útil para buscar certificaciones por nombre en lugar de ID.
     * La búsqueda es case-insensitive para mayor flexibilidad.
     * 
     * @param nombreSello Nombre del sello de certificación
     * @return ResponseEntity con la certificación encontrada (CertificacionResponseDTO) y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/certificaciones/sello/Fair Trade
     */
    @GetMapping("/sello/{nombreSello}")
    public ResponseEntity<CertificacionResponseDTO> getCertificacionByNombreSello(@PathVariable String nombreSello) {
        // @PathVariable extrae el valor del nombreSello desde la URL
        CertificacionResponseDTO certificacion = certificacionService.getCertificacionByNombreSello(nombreSello);
        return ResponseEntity.ok(certificacion);
    }

    /**
     * Actualiza una certificación existente en la base de datos.
     * 
     * Este endpoint permite actualizaciones parciales. Solo se actualizan los campos
     * que se envíen en el body. Los campos no incluidos se mantienen sin cambios.
     * 
     * @param id Identificador único de la certificación a actualizar (en la URL)
     * @param dto Datos a actualizar (todos los campos son opcionales)
     * @return ResponseEntity con la certificación actualizada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     * @throws com.ecoshop.exception.BadRequestException si el nuevo nombre de sello ya existe en otra certificación
     * 
     * Ejemplo de uso - Actualización parcial (solo imagenUrl):
     * PUT http://localhost:8080/api/v1/certificaciones/2
     * Body: {
     *   "imagenUrl": "https://i.ibb.co/xxxxx/carbon-neutral.png"
     * }
     * 
     * Ejemplo de uso - Actualización completa:
     * PUT http://localhost:8080/api/v1/certificaciones/1
     * Body: {
     *   "nombreSello": "Fair Trade Actualizado",
     *   "descripcion": "Nueva descripción",
     *   "entidadEmisora": "Fair Trade International",
     *   "imagenUrl": "https://i.ibb.co/xxxxx/fair-trade.png"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<CertificacionResponseDTO> updateCertificacion(
            @PathVariable Integer id, 
            @RequestBody CertificacionRequestDTO dto) {
        // @PathVariable extrae el ID desde la URL
        // @RequestBody convierte el JSON del cuerpo de la petición a un objeto CertificacionRequestDTO
        // Nota: No usamos @Valid para permitir actualizaciones parciales (todos los campos opcionales, esto con la finalidad de que sea más sencilla la actualización en el frontend)
        CertificacionResponseDTO updatedCertificacion = certificacionService.updateCertificacion(id, dto);
        return ResponseEntity.ok(updatedCertificacion);
    }

    /**
     * Elimina una certificación de la base de datos.
     * 
     * @param id Identificador único de la certificación a eliminar
     * @return ResponseEntity vacío con código HTTP 204 (NO CONTENT)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     * 
     * Ejemplo de uso:
     * DELETE http://localhost:8080/api/v1/certificaciones/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificacion(@PathVariable Integer id) {
        // @PathVariable extrae el ID desde la URL
        certificacionService.deleteCertificacion(id);
        // Retornamos código HTTP 204 (NO CONTENT) para indicar que la operación fue exitosa
        // pero no hay contenido en la respuesta
        return ResponseEntity.noContent().build();
    }
}

