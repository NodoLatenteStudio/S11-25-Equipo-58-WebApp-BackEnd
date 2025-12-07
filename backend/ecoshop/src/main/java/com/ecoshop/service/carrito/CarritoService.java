package com.ecoshop.service.carrito;

import com.ecoshop.dto.Carrito.CarritoItemRequestDTO;
import com.ecoshop.dto.Carrito.CarritoResponseDTO;
import com.ecoshop.dto.Carrito.ImpactoAmbientalCarritoResponse;

/**
 * Interfaz del servicio para gestionar carritos de compras.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre carritos.
 * La implementación se encuentra en CarritoServiceImpl.
 * 
 * Responsabilidades:
 * - Obtener o crear el carrito del usuario autenticado
 * - Agregar items al carrito
 * - Actualizar cantidad de items
 * - Eliminar items del carrito
 * - Vaciar el carrito
 * - Calcular el impacto ambiental del carrito
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 */
public interface CarritoService {

    /**
     * Obtiene el carrito del usuario autenticado.
     * Si no existe, lo crea automáticamente.
     * 
     * @param usuarioId ID del usuario autenticado
     * @return CarritoResponseDTO con el carrito y sus items
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     */
    CarritoResponseDTO obtenerCarrito(Integer usuarioId);

    /**
     * Agrega un item al carrito del usuario.
     * Si el producto ya está en el carrito, actualiza la cantidad.
     * 
     * @param usuarioId ID del usuario autenticado
     * @param request Datos del item a agregar (productoId, cantidad)
     * @return CarritoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario o producto no existen
     */
    CarritoResponseDTO agregarItem(Integer usuarioId, CarritoItemRequestDTO request);

    /**
     * Actualiza la cantidad de un item del carrito.
     * 
     * @param usuarioId ID del usuario autenticado
     * @param itemId ID del item a actualizar
     * @param cantidad Nueva cantidad (debe ser >= 1)
     * @return CarritoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el carrito o item no existen
     * @throws com.ecoshop.exception.BadRequestException si la cantidad es inválida
     */
    CarritoResponseDTO actualizarCantidadItem(Integer usuarioId, Integer itemId, Integer cantidad);

    /**
     * Elimina un item del carrito.
     * 
     * @param usuarioId ID del usuario autenticado
     * @param itemId ID del item a eliminar
     * @return CarritoResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el carrito o item no existen
     */
    CarritoResponseDTO eliminarItem(Integer usuarioId, Integer itemId);

    /**
     * Vacía el carrito del usuario (elimina todos los items).
     * 
     * @param usuarioId ID del usuario autenticado
     * @return CarritoResponseDTO vacío
     * @throws com.ecoshop.exception.ResourceNotFoundException si el carrito no existe
     */
    CarritoResponseDTO vaciarCarrito(Integer usuarioId);

    /**
     * Elimina el carrito completo del usuario (elimina la entidad Carrito y todos sus items).
     * 
     * @param usuarioId ID del usuario autenticado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el carrito no existe
     * @throws com.ecoshop.exception.ForbiddenException si el carrito no pertenece al usuario
     */
    void eliminarCarrito(Integer usuarioId);

    /**
     * Calcula el impacto ambiental del carrito actual.
     * 
     * @param usuarioId ID del usuario autenticado
     * @return ImpactoAmbientalCarritoResponse con huella de carbono y equivalencias
     * @throws com.ecoshop.exception.ResourceNotFoundException si el carrito no existe
     */
    ImpactoAmbientalCarritoResponse calcularImpactoAmbiental(Integer usuarioId);
}

