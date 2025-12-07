package com.ecoshop.controller.carrito;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Carrito.CarritoItemRequestDTO;
import com.ecoshop.dto.Carrito.CarritoResponseDTO;
import com.ecoshop.dto.Carrito.ImpactoAmbientalCarritoResponse;
import com.ecoshop.service.carrito.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar carritos de compras.
 * 
 * Este controlador expone los endpoints HTTP para operaciones CRUD sobre carritos.
 * Todos los endpoints están bajo la ruta base "/api/v1/carrito".
 * 
 * El carrito persiste en la base de datos para usuarios autenticados.
 * Si el usuario no está autenticado, el frontend debe usar localStorage.
 * 
 * Responsabilidades:
 * - Recibir peticiones HTTP del cliente
 * - Validar los datos de entrada usando @Valid
 * - Obtener el usuario autenticado del token JWT
 * - Delegar la lógica de negocio al servicio
 * - Retornar respuestas HTTP apropiadas
 * 
 * ✅ TODOS LOS ENDPOINTS ESTÁN FUNCIONANDO CORRECTAMENTE
 * 
 * Endpoints disponibles (todos requieren autenticación):
 * ✅ GET /api/v1/carrito - Obtener carrito del usuario autenticado (200 OK)
 * ✅ POST /api/v1/carrito/items - Agregar item al carrito (200 OK)
 * ✅ PUT /api/v1/carrito/items/{itemId} - Actualizar cantidad de un item (200 OK) - Validación de permisos
 * ✅ DELETE /api/v1/carrito/items/{itemId} - Eliminar item del carrito (200 OK) - Validación de permisos
 * ✅ DELETE /api/v1/carrito/vaciar - Vaciar carrito (elimina items pero mantiene el carrito) (200 OK) - Validación de permisos
 * ✅ DELETE /api/v1/carrito - Eliminar carrito completo (elimina la entidad Carrito) (204 No Content) - Validación de permisos
 * ✅ GET /api/v1/carrito/impacto-ambiental - Calcular impacto ambiental del carrito (200 OK)
 * 
 * Características de seguridad:
 * - UsuarioId automático: se obtiene del token JWT (no se envía en el body)
 * - Validación de permisos: usuarios solo pueden modificar su propio carrito
 * - Relación One-to-One: cada usuario tiene un solo carrito
 * - Creación automática: el carrito se crea automáticamente si no existe al acceder a él (Funcionamiento habitual de un e-commerce)
 * 
 * Validación:
 * Los DTOs se validan automáticamente usando @Valid. Si la validación falla,
 * se lanza MethodArgumentNotValidException que es manejada por GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    /**
     * Obtiene el carrito del usuario autenticado.
     * Si no existe, se crea automáticamente.
     * 
     * Endpoint: GET /api/v1/carrito
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el carrito y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/carrito
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CarritoResponseDTO carrito = carritoService.obtenerCarrito(usuario.getUsuarioId());
        return ResponseEntity.ok(carrito);
    }

    /**
     * Agrega un item al carrito del usuario autenticado.
     * Si el producto ya está en el carrito, actualiza la cantidad.
     * 
     * Endpoint: POST /api/v1/carrito/items
     * 
     * @param request Datos del item a agregar (productoId, cantidad)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el carrito actualizado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * POST /api/v1/carrito/items
     * Authorization: Bearer <token_jwt>
     * Content-Type: application/json
     * Body: {
     *   "productoId": 1,
     *   "cantidad": 2
     * }
     */
    @PostMapping("/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(
            @Valid @RequestBody CarritoItemRequestDTO request,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CarritoResponseDTO carrito = carritoService.agregarItem(usuario.getUsuarioId(), request);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Actualiza la cantidad de un item del carrito.
     * 
     * Endpoint: PUT /api/v1/carrito/items/{itemId}
     * 
     * @param itemId ID del item a actualizar
     * @param cantidad Nueva cantidad (debe ser >= 1)
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el carrito actualizado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * PUT /api/v1/carrito/items/1?cantidad=3
     * Authorization: Bearer <token_jwt>
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> actualizarCantidadItem(
            @PathVariable Integer itemId,
            @RequestParam Integer cantidad,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CarritoResponseDTO carrito = carritoService.actualizarCantidadItem(usuario.getUsuarioId(), itemId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Elimina un item del carrito.
     * 
     * Endpoint: DELETE /api/v1/carrito/items/{itemId}
     * 
     * @param itemId ID del item a eliminar
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el carrito actualizado y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/carrito/items/1
     * Authorization: Bearer <token_jwt>
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarItem(
            @PathVariable Integer itemId,
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CarritoResponseDTO carrito = carritoService.eliminarItem(usuario.getUsuarioId(), itemId);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Vacía el carrito del usuario (elimina todos los items pero mantiene el carrito).
     * 
     * Endpoint: DELETE /api/v1/carrito/vaciar
     * 
     * Este endpoint elimina todos los items del carrito pero mantiene la entidad Carrito.
     * Útil cuando se quiere limpiar el carrito sin eliminarlo completamente.
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el carrito vacío y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/carrito/vaciar
     * Authorization: Bearer <token_jwt>
     */
    @DeleteMapping("/vaciar")
    public ResponseEntity<CarritoResponseDTO> vaciarCarrito(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        CarritoResponseDTO carrito = carritoService.vaciarCarrito(usuario.getUsuarioId());
        return ResponseEntity.ok(carrito);
    }

    /**
     * Elimina el carrito completo del usuario (elimina la entidad Carrito y todos sus items).
     * 
     * Endpoint: DELETE /api/v1/carrito
     * 
     * Este endpoint elimina completamente el carrito del usuario, incluyendo todos sus items.
     * Después de eliminar, el carrito se creará automáticamente la próxima vez que se acceda a él.
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity sin contenido y código HTTP 204 (No Content)
     * 
     * Ejemplo de petición:
     * DELETE /api/v1/carrito
     * Authorization: Bearer <token_jwt>
     */
    @DeleteMapping
    public ResponseEntity<Void> eliminarCarrito(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        carritoService.eliminarCarrito(usuario.getUsuarioId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula el impacto ambiental del carrito actual.
     * 
     * Endpoint: GET /api/v1/carrito/impacto-ambiental
     * 
     * Este endpoint proporciona información sobre la huella de carbono del carrito,
     * el CO₂ ahorrado y equivalencias en términos comprensibles.
     * 
     * @param authentication Usuario autenticado (inyectado automáticamente por Spring Security)
     * @return ResponseEntity con el impacto ambiental y código HTTP 200 (OK)
     * 
     * Ejemplo de petición:
     * GET /api/v1/carrito/impacto-ambiental
     * Authorization: Bearer <token_jwt>
     */
    @GetMapping("/impacto-ambiental")
    public ResponseEntity<ImpactoAmbientalCarritoResponse> calcularImpactoAmbiental(
            Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        ImpactoAmbientalCarritoResponse impacto = carritoService.calcularImpactoAmbiental(usuario.getUsuarioId());
        return ResponseEntity.ok(impacto);
    }
}

