package com.ecoshop.controller.eco;

import com.ecoshop.dto.Recompensa.RecompensaRequestDTO;
import com.ecoshop.dto.Recompensa.RecompensaResponseDTO;
import com.ecoshop.service.eco.RecompensaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar recompensas del catálogo de Eco-Wallet.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre recompensas.
 * Todos los endpoints están bajo la ruta base "/api/v1/recompensas".
 * 
 * ✅ Todos los endpoints están funcionando correctamente.
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * 
 * Convenciones REST:
 * - POST /api/v1/recompensas: Crear una nueva recompensa (201 Created) ✅
 * - GET /api/v1/recompensas/{id}: Obtener una recompensa por ID (200 OK) ✅
 * - GET /api/v1/recompensas: Obtener todas las recompensas (200 OK) ✅
 * - GET /api/v1/recompensas/activas: Obtener recompensas activas (200 OK) ✅
 * - GET /api/v1/recompensas/tipo/{tipo}: Obtener recompensas por tipo (200 OK) ✅
 * - GET /api/v1/recompensas/disponibles?puntos={puntos}: Obtener recompensas disponibles por puntos (200 OK) ✅
 * - PUT /api/v1/recompensas/{id}: Actualizar una recompensa existente (200 OK) ✅
 * - DELETE /api/v1/recompensas/{id}: Eliminar una recompensa (204 No Content) ✅
 */
@RestController
@RequestMapping("/api/v1/recompensas")
@RequiredArgsConstructor
public class RecompensaController {

    private final RecompensaService recompensaService;

    /**
     * Crea una nueva recompensa en el catálogo.
     * 
     * Endpoint: POST /api/v1/recompensas
     * ✅ Funcionando correctamente.
     * 
     * @param dto Datos de la recompensa a crear (validados automáticamente)
     * @return ResponseEntity con la recompensa creada y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/recompensas
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Descuento 10%",
     *   "descripcion": "Descuento del 10% en tu próxima compra",
     *   "puntosRequeridos": 100,
     *   "tipo": "descuento",
     *   "valor": 10.0,
     *   "stockDisponible": 50,
     *   "imagenUrl": "https://example.com/descuento.png",
     *   "activo": true
     * }
     */
    @PostMapping
    public ResponseEntity<RecompensaResponseDTO> createRecompensa(@Valid @RequestBody RecompensaRequestDTO dto) {
        RecompensaResponseDTO newRecompensa = recompensaService.createRecompensa(dto);
        return new ResponseEntity<>(newRecompensa, HttpStatus.CREATED);
    }

    /**
     * Obtiene una recompensa por su ID.
     * 
     * Endpoint: GET /api/v1/recompensas/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador de la recompensa
     * @return ResponseEntity con la recompensa encontrada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/recompensas/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecompensaResponseDTO> getRecompensaById(@PathVariable Integer id) {
        return ResponseEntity.ok(recompensaService.getRecompensaById(id));
    }

    /**
     * Obtiene todas las recompensas.
     * 
     * Endpoint: GET /api/v1/recompensas
     * ✅ Funcionando correctamente.
     * 
     * @return ResponseEntity con la lista de recompensas y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/recompensas
     */
    @GetMapping
    public ResponseEntity<List<RecompensaResponseDTO>> getAllRecompensas() {
        return ResponseEntity.ok(recompensaService.getAllRecompensas());
    }

    /**
     * Obtiene todas las recompensas activas.
     * 
     * Endpoint: GET /api/v1/recompensas/activas
     * ✅ Funcionando correctamente.
     * 
     * @return ResponseEntity con la lista de recompensas activas y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/recompensas/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<RecompensaResponseDTO>> getRecompensasActivas() {
        return ResponseEntity.ok(recompensaService.getRecompensasActivas());
    }

    /**
     * Obtiene recompensas por tipo.
     * 
     * Endpoint: GET /api/v1/recompensas/tipo/{tipo}
     * ✅ Funcionando correctamente.
     * 
     * @param tipo Tipo de recompensa (ej: "descuento", "envio_gratis", "producto")
     * @return ResponseEntity con la lista de recompensas del tipo especificado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/recompensas/tipo/descuento
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<RecompensaResponseDTO>> getRecompensasByTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(recompensaService.getRecompensasByTipo(tipo));
    }

    /**
     * Obtiene recompensas disponibles según los puntos del usuario.
     * 
     * Endpoint: GET /api/v1/recompensas/disponibles?puntos={puntos}
     * ✅ Funcionando correctamente.
     * 
     * Este endpoint retorna todas las recompensas activas cuyo costo en puntos (puntosRequeridos)
     * sea menor o igual a los puntos disponibles del usuario. Es decir, muestra todas las
     * recompensas que el usuario PUEDE canjear con sus puntos actuales, no solo las que requieren
     * exactamente esa cantidad.
     * 
     * Ejemplo: Si el usuario tiene 150 puntos, verá:
     * - Recompensas que requieren 100 puntos (puede canjearlas)
     * - Recompensas que requieren 150 puntos (puede canjearlas)
     * - NO verá recompensas que requieren 200 puntos (no puede canjearlas)
     * 
     * Las recompensas se ordenan de menor a mayor costo en puntos.
     * 
     * @param puntos Puntos máximos disponibles del usuario
     * @return ResponseEntity con la lista de recompensas que se pueden canjear y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/recompensas/disponibles?puntos=150
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<RecompensaResponseDTO>> getRecompensasDisponiblesPorPuntos(
            @RequestParam Integer puntos) {
        return ResponseEntity.ok(recompensaService.getRecompensasDisponiblesPorPuntos(puntos));
    }

    /**
     * Actualiza una recompensa existente.
     * 
     * Endpoint: PUT /api/v1/recompensas/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador de la recompensa a actualizar
     * @param dto Nuevos datos de la recompensa (validados automáticamente)
     * @return ResponseEntity con la recompensa actualizada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/recompensas/1
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Descuento 15%",
     *   "puntosRequeridos": 150
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecompensaResponseDTO> updateRecompensa(
            @PathVariable Integer id,
            @Valid @RequestBody RecompensaRequestDTO dto) {
        return ResponseEntity.ok(recompensaService.updateRecompensa(id, dto));
    }

    /**
     * Elimina una recompensa.
     * 
     * Endpoint: DELETE /api/v1/recompensas/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador de la recompensa a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la recompensa no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/recompensas/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecompensa(@PathVariable Integer id) {
        recompensaService.deleteRecompensa(id);
        return ResponseEntity.noContent().build();
    }
}

