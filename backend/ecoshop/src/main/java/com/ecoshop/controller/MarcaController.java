package com.ecoshop.controller;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Marca.MarcaRequestDTO;
import com.ecoshop.dto.Marca.MarcaResponseDTO;
import com.ecoshop.service.MarcaService;
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
 * Convenciones REST:
 * - POST /api/v1/marcas: Crear una nueva marca (201 Created)
 * - GET /api/v1/marcas/{id}: Obtener una marca por ID (200 OK)
 * - GET /api/v1/marcas: Obtener todas las marcas (200 OK)
 * - PUT /api/v1/marcas/{id}: Actualizar una marca existente (200 OK)
 * - DELETE /api/v1/marcas/{id}: Eliminar una marca (204 No Content)
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
    public ResponseEntity<Void> deleteMarca(@PathVariable Integer id) {
        marcaService.deleteMarca(id);
        return ResponseEntity.noContent().build();
    }
}
