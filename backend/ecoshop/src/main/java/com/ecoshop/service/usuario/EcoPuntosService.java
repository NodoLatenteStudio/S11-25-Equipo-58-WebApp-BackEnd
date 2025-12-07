package com.ecoshop.service.usuario;

import com.ecoshop.domain.Pedido;
import com.ecoshop.dto.EcoPuntos.EcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.HistorialEcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.CalculoEcoPuntosResponse;

import java.util.List;

/**
 * Interfaz del servicio para gestionar eco-puntos.
 * 
 * Este servicio se encarga de calcular, asignar y gestionar los eco-puntos
 * de los usuarios basándose en sus compras sostenibles.
 * 
 * Responsabilidades:
 * - Calcular puntos ganados por una compra
 * - Asignar puntos automáticamente al confirmar pedidos
 * - Gestionar niveles de eco-puntos
 * - Proporcionar historial de puntos
 */
public interface EcoPuntosService {

    /**
     * Calcula los eco-puntos que se ganarían con un pedido antes de finalizarlo.
     * 
     * Este método permite mostrar al usuario cuántos puntos ganará antes de confirmar la compra.
     * 
     * @param pedidoId ID del pedido para calcular puntos
     * @return CalculoEcoPuntosResponse con el cálculo de puntos
     */
    CalculoEcoPuntosResponse calcularPuntosPedido(Integer pedidoId);

    /**
     * Obtiene el estado actual de eco-puntos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return EcoPuntosResponse con el estado actual de eco-puntos
     */
    EcoPuntosResponse obtenerEcoPuntos(Integer usuarioId);

    /**
     * Obtiene el historial de eco-puntos ganados por un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return HistorialEcoPuntosResponse con el historial de puntos
     */
    HistorialEcoPuntosResponse obtenerHistorialEcoPuntos(Integer usuarioId);

    /**
     * Calcula y asigna eco-puntos a un usuario cuando se confirma un pedido.
     * 
     * Este método se llama automáticamente cuando un pedido cambia a estado "entregado".
     * 
     * @param pedido Pedido confirmado
     * @return Cantidad de puntos ganados
     */
    Integer asignarPuntosPorPedido(Pedido pedido);

    /**
     * Calcula los puntos que se ganarían con una lista de items de pedido.
     * 
     * @param items Lista de items del pedido (productoId, cantidad)
     * @return Cantidad de puntos calculados
     */
    Integer calcularPuntosItems(List<CalculoEcoPuntosResponse.ItemPedido> items);
}

