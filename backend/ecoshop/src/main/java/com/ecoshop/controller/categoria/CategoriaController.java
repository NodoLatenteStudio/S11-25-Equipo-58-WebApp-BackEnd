package com.ecoshop.controller.categoria;

import com.ecoshop.dto.Categoria.CategoriaRequestDTO;
import com.ecoshop.dto.Categoria.CategoriaResponseDTO;
import com.ecoshop.service.categoria.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar categorías.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre categorías.
 * Todos los endpoints están bajo la ruta base "/api/v1/categorias".
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Convenciones REST:
 * ✅ POST /api/v1/categorias: Crear una nueva categoría (201 Created) - Requiere autenticación
 * ✅ GET /api/v1/categorias/{id}: Obtener una categoría por ID (200 OK) - Requiere autenticación
 * ✅ GET /api/v1/categorias: Obtener todas las categorías (200 OK) - Requiere autenticación
 * ✅ PUT /api/v1/categorias/{id}: Actualizar una categoría existente (200 OK) - Requiere autenticación
 * ✅ DELETE /api/v1/categorias/{id}: Eliminar una categoría (204 No Content) - Requiere autenticación
 * 
 * Nota: Todos los endpoints de categorías requieren autenticación según la configuración
 * actual en SecurityConfig (caen bajo .anyRequest().authenticated()).
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Crea una nueva categoría.
     * 
     * Endpoint: POST /api/v1/categorias
     * 
     * @param dto Datos de la categoría a crear (validados automáticamente)
     * @return ResponseEntity con la categoría creada y código HTTP 201 (Created)
     * 
     * Ejemplo de petición:
     * POST /api/v1/categorias
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Ropa",
     *   "descripcion": "Productos de vestimenta sostenible"
     * }
     */
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> createCategoria(@Valid @RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO newCategoria = categoriaService.createCategoria(dto);
        return new ResponseEntity<>(newCategoria, HttpStatus.CREATED);
    }

    /**
     * Obtiene una categoría por su ID.
     * 
     * Endpoint: GET /api/v1/categorias/{id}
     * 
     * @param id Identificador de la categoría
     * @return ResponseEntity con la categoría encontrada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/categorias/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.getCategoriaById(id));
    }

    /**
     * Obtiene todas las categorías.
     * 
     * Endpoint: GET /api/v1/categorias
     * 
     * @return ResponseEntity con la lista de categorías y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/categorias
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.getAllCategorias());
    }

    /**
     * Actualiza una categoría existente.
     * 
     * Endpoint: PUT /api/v1/categorias/{id}
     * 
     * @param id Identificador de la categoría a actualizar
     * @param dto Nuevos datos de la categoría (validados automáticamente)
     * @return ResponseEntity con la categoría actualizada y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     * 
     * Ejemplo de petición:
     * PUT /api/v1/categorias/1
     * Content-Type: application/json
     * Body: {
     *   "nombre": "Ropa Actualizada",
     *   "descripcion": "Nueva descripción"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> updateCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.updateCategoria(id, dto));
    }

    /**
     * Elimina una categoría.
     * 
     * Endpoint: DELETE /api/v1/categorias/{id}
     * 
     * @param id Identificador de la categoría a eliminar
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/categorias/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Integer id) {
        categoriaService.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }
}

