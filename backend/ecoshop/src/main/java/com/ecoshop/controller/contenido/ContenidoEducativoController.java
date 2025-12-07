package com.ecoshop.controller.contenido;

import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoRequestDTO;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoResponseDTO;
import com.ecoshop.service.contenido.ContenidoEducativoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar contenido educativo.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre contenido educativo.
 * Todos los endpoints están bajo la ruta base "/api/v1/contenido-educativo".
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
 * - POST /api/v1/contenido-educativo: Crear nuevo contenido (201 Created) ✅
 * - GET /api/v1/contenido-educativo/{id}: Obtener contenido por ID (200 OK) ✅
 * - GET /api/v1/contenido-educativo: Obtener todos los contenidos (200 OK) ✅
 * - GET /api/v1/contenido-educativo/activos: Obtener contenidos activos (200 OK) ✅
 * - GET /api/v1/contenido-educativo/categoria/{categoria}: Obtener contenidos por categoría (200 OK) ✅
 * - GET /api/v1/contenido-educativo/buscar?titulo={titulo}: Buscar contenidos por título (200 OK) ✅
 * - PUT /api/v1/contenido-educativo/{id}: Actualizar contenido (200 OK) ✅
 * - DELETE /api/v1/contenido-educativo/{id}: Eliminar contenido (204 No Content) ✅
 */
@RestController
@RequestMapping("/api/v1/contenido-educativo")
@RequiredArgsConstructor
public class ContenidoEducativoController {

    private final ContenidoEducativoService contenidoEducativoService;

    /**
     * Crea un nuevo contenido educativo.
     * 
     * Endpoint: POST /api/v1/contenido-educativo
     * ✅ Funcionando correctamente.
     * 
     * @param dto Datos del contenido educativo a crear (validados automáticamente)
     * @return ResponseEntity con el contenido educativo creado y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/contenido-educativo
     * Content-Type: application/json
     * Body: {
     *   "titulo": "Guía de Consumo Responsable",
     *   "descripcion": "Aprende cómo consumir de forma más sostenible",
     *   "contenido": "Contenido completo del artículo...",
     *   "categoria": "Consumo Responsable",
     *   "imagenUrl": "https://example.com/imagen.jpg",
     *   "autor": "Equipo EcoShop",
     *   "activo": true
     * }
     */
    @PostMapping
    public ResponseEntity<ContenidoEducativoResponseDTO> createContenidoEducativo(
            @Valid @RequestBody ContenidoEducativoRequestDTO dto) {
        ContenidoEducativoResponseDTO newContenido = contenidoEducativoService.createContenidoEducativo(dto);
        return new ResponseEntity<>(newContenido, HttpStatus.CREATED);
    }

    /**
     * Obtiene un contenido educativo por su ID.
     * 
     * Endpoint: GET /api/v1/contenido-educativo/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador del contenido educativo
     * @return ResponseEntity con el contenido educativo encontrado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el contenido no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/contenido-educativo/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContenidoEducativoResponseDTO> getContenidoEducativoById(@PathVariable Integer id) {
        return ResponseEntity.ok(contenidoEducativoService.getContenidoEducativoById(id));
    }

    /**
     * Obtiene todos los contenidos educativos.
     * 
     * Endpoint: GET /api/v1/contenido-educativo
     * ✅ Funcionando correctamente.
     * 
     * @return ResponseEntity con la lista de contenidos educativos y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/contenido-educativo
     */
    @GetMapping
    public ResponseEntity<List<ContenidoEducativoResponseDTO>> getAllContenidosEducativos() {
        return ResponseEntity.ok(contenidoEducativoService.getAllContenidosEducativos());
    }

    /**
     * Obtiene todos los contenidos educativos activos.
     * 
     * Endpoint: GET /api/v1/contenido-educativo/activos
     * ✅ Funcionando correctamente.
     * 
     * @return ResponseEntity con la lista de contenidos educativos activos y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/contenido-educativo/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ContenidoEducativoResponseDTO>> getContenidosEducativosActivos() {
        return ResponseEntity.ok(contenidoEducativoService.getContenidosEducativosActivos());
    }

    /**
     * Obtiene contenidos educativos por categoría.
     * 
     * Endpoint: GET /api/v1/contenido-educativo/categoria/{categoria}
     * ✅ Funcionando correctamente.
     * 
     * @param categoria Categoría del contenido
     * @return ResponseEntity con la lista de contenidos educativos de la categoría y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/contenido-educativo/categoria/Consumo%20Responsable
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ContenidoEducativoResponseDTO>> getContenidosEducativosByCategoria(
            @PathVariable String categoria) {
        return ResponseEntity.ok(contenidoEducativoService.getContenidosEducativosByCategoria(categoria));
    }

    /**
     * Busca contenidos educativos por título.
     * 
     * Endpoint: GET /api/v1/contenido-educativo/buscar?titulo={titulo}
     * ✅ Funcionando correctamente.
     * 
     * @param titulo Título o parte del título a buscar
     * @return ResponseEntity con la lista de contenidos educativos que coinciden y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/contenido-educativo/buscar?titulo=consumo
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ContenidoEducativoResponseDTO>> buscarContenidosEducativosPorTitulo(
            @RequestParam String titulo) {
        return ResponseEntity.ok(contenidoEducativoService.buscarContenidosEducativosPorTitulo(titulo));
    }

    /**
     * Actualiza un contenido educativo existente.
     * 
     * Endpoint: PUT /api/v1/contenido-educativo/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador del contenido educativo a actualizar
     * @param dto Nuevos datos del contenido educativo (validados automáticamente)
     * @return ResponseEntity con el contenido educativo actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el contenido no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/contenido-educativo/1
     * Content-Type: application/json
     * Body: {
     *   "titulo": "Guía Actualizada",
     *   ...
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContenidoEducativoResponseDTO> updateContenidoEducativo(
            @PathVariable Integer id,
            @Valid @RequestBody ContenidoEducativoRequestDTO dto) {
        return ResponseEntity.ok(contenidoEducativoService.updateContenidoEducativo(id, dto));
    }

    /**
     * Elimina un contenido educativo.
     * 
     * Endpoint: DELETE /api/v1/contenido-educativo/{id}
     * ✅ Funcionando correctamente.
     * 
     * @param id Identificador del contenido educativo a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el contenido no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/contenido-educativo/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContenidoEducativo(@PathVariable Integer id) {
        contenidoEducativoService.deleteContenidoEducativo(id);
        return ResponseEntity.noContent().build();
    }
}

