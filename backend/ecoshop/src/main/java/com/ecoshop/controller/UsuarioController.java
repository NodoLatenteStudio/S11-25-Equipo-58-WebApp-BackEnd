package com.ecoshop.controller;

import com.ecoshop.dto.Usuario.UsuarioRequestDTO;
import com.ecoshop.dto.Usuario.UsuarioResponseDTO;
import com.ecoshop.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Convenciones REST:
 * - POST /api/v1/usuarios: Crear un nuevo usuario (201 Created)
 * - GET /api/v1/usuarios/{id}: Obtener un usuario por ID (200 OK)
 * - GET /api/v1/usuarios: Obtener todos los usuarios (200 OK)
 * - PUT /api/v1/usuarios/{id}: Actualizar un usuario existente (200 OK)
 * - DELETE /api/v1/usuarios/{id}: Eliminar un usuario (204 No Content)
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
     * @param id Identificador del usuario
     * @return ResponseEntity con el usuario encontrado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de petición:
     * GET /api/v1/usuarios/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Integer id) {
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
     * 
     * @param id Identificador del usuario a actualizar
     * @param dto Nuevos datos del usuario (validados automáticamente)
     * @return ResponseEntity con el usuario actualizado y código HTTP 200 (OK)
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
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
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));
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
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
