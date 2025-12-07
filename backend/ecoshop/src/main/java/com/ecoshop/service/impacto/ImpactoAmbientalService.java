package com.ecoshop.service.impacto;

import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoProductoResponse;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoPedidoResponse;
import com.ecoshop.dto.ImpactoAmbiental.ComparacionProductosResponse;
import com.ecoshop.enums.EcoBadge;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz del servicio para calcular el impacto ambiental.
 * 
 * Este servicio se encarga de calcular la huella de carbono de productos y pedidos,
 * considerando múltiples factores como fabricación, materiales, transporte, empaque y entrega.
 * 
 * Responsabilidades:
 * - Calcular huella de carbono de productos individuales
 * - Calcular huella de carbono de pedidos completos
 * - Comparar impacto ambiental entre productos
 * - Proporcionar métricas detalladas de impacto
 */
public interface ImpactoAmbientalService {

    /**
     * Calcula la huella de carbono total de un producto.
     * 
     * Considera los siguientes factores:
     * - Emisiones por fabricación (base según tipo de producto)
     * - Emisiones por materiales utilizados
     * - Emisiones por transporte (distancia desde origen)
     * - Emisiones por empaque
     * 
     * @param producto Producto para calcular la huella de carbono
     * @return BigDecimal con la huella de carbono total en kg CO₂
     */
    BigDecimal calcularHuellaCarbonoProducto(Producto producto);

    /**
     * Calcula la huella de carbono total de un pedido.
     * 
     * Suma la huella de carbono de todos los productos en el pedido
     * (considerando la cantidad de cada producto) y agrega emisiones
     * de transporte y entrega.
     * 
     * @param pedido Pedido para calcular la huella de carbono
     * @return BigDecimal con la huella de carbono total en kg CO₂
     */
    BigDecimal calcularHuellaCarbonoPedido(Pedido pedido);

    /**
     * Calcula el impacto ambiental detallado de un producto.
     * 
     * Proporciona un desglose completo de las emisiones por categoría.
     * 
     * @param productoId ID del producto
     * @return CalculoImpactoProductoResponse con el desglose detallado
     */
    CalculoImpactoProductoResponse calcularImpactoProducto(Integer productoId);

    /**
     * Calcula el impacto ambiental detallado de un pedido.
     * 
     * Proporciona un desglose completo de las emisiones del pedido.
     * 
     * @param pedidoId ID del pedido
     * @return CalculoImpactoPedidoResponse con el desglose detallado
     */
    CalculoImpactoPedidoResponse calcularImpactoPedido(Integer pedidoId);

    /**
     * Compara el impacto ambiental entre múltiples productos.
     * 
     * @param productoIds Lista de IDs de productos a comparar
     * @return ComparacionProductosResponse con la comparación detallada
     */
    ComparacionProductosResponse compararProductos(List<Integer> productoIds);

    /**
     * Calcula el CO₂ ahorrado comparado con un producto convencional.
     * 
     * @param producto Producto para calcular el ahorro
     * @return BigDecimal con el CO₂ ahorrado en kg
     */
    BigDecimal calcularCO2Ahorrado(Producto producto);

    /**
     * Calcula el eco-badge (nivel de impacto ambiental) de un producto.
     * 
     * El cálculo considera:
     * - Huella de carbono total
     * - Porcentaje de material reciclable
     * - Distancia de transporte (origen)
     * - Cantidad y tipo de certificaciones
     * 
     * @param producto Producto para calcular el eco-badge
     * @return EcoBadge calculado (BAJO_IMPACTO, MEDIO_IMPACTO, o NEUTRO)
     */
    EcoBadge calcularEcoBadge(Producto producto);

    /**
     * Calcula y almacena todas las métricas ambientales detalladas de un producto.
     * 
     * Este método calcula todas las métricas y las almacena en la entidad Producto:
     * - Emisiones por fabricación
     * - Emisiones por materiales
     * - Emisiones por transporte
     * - Emisiones por empaque
     * - Emisiones por entrega
     * - Consumo de agua
     * - Distancia de transporte
     * - CO₂ ahorrado vs convencional
     * 
     * @param producto Producto para calcular y actualizar métricas
     * @return Producto con todas las métricas calculadas y almacenadas
     */
    Producto calcularYAlmacenarMetricasDetalladas(Producto producto);

    /**
     * Obtiene las métricas ambientales detalladas de un producto.
     * 
     * @param productoId ID del producto
     * @return MetricasAmbientalesResponse con todas las métricas detalladas
     */
    com.ecoshop.dto.ImpactoAmbiental.MetricasAmbientalesResponse obtenerMetricasAmbientales(Integer productoId);

    /**
     * Obtiene el dashboard completo de impacto ambiental de un usuario.
     * 
     * Calcula y agrega todas las métricas de los pedidos del usuario:
     * - CO₂ ahorrado total
     * - Agua ahorrada total
     * - Compras sostenibles
     * - Material reciclado total
     * - Eco-puntos (si está implementado)
     * - Objetivos de sostenibilidad
     * 
     * @param usuarioId ID del usuario
     * @return DashboardImpactoResponse con todas las métricas agregadas
     */
    com.ecoshop.dto.Usuario.DashboardImpactoResponse obtenerDashboardImpacto(Integer usuarioId);

    /**
     * Obtiene las métricas ambientales agregadas de un usuario (versión simplificada).
     * 
     * @param usuarioId ID del usuario
     * @return MetricasAmbientalesUsuarioResponse con las métricas agregadas
     */
    com.ecoshop.dto.Usuario.MetricasAmbientalesUsuarioResponse obtenerMetricasAmbientalesUsuario(Integer usuarioId);

    /**
     * Obtiene el historial de impacto ambiental del usuario con filtros de fecha.
     * 
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del rango (opcional)
     * @param fechaFin Fecha de fin del rango (opcional)
     * @return HistorialImpactoResponse con el historial detallado
     */
    com.ecoshop.dto.Usuario.HistorialImpactoResponse obtenerHistorialImpacto(
            Integer usuarioId, 
            java.time.LocalDateTime fechaInicio, 
            java.time.LocalDateTime fechaFin);

    /**
     * Obtiene las tendencias de impacto ambiental del usuario para gráficas.
     * 
     * Agrega los datos por período (día, semana o mes) para facilitar la visualización
     * en gráficas de tendencias.
     * 
     * @param usuarioId ID del usuario
     * @param periodo Período de agregación: "dia", "semana", "mes"
     * @param mesesAtras Número de meses hacia atrás para obtener datos (por defecto 6)
     * @return TendenciasImpactoResponse con datos agregados por período
     */
    com.ecoshop.dto.Usuario.TendenciasImpactoResponse obtenerTendenciasImpacto(
            Integer usuarioId, 
            String periodo, 
            Integer mesesAtras);

    /**
     * Obtiene la trazabilidad completa de un producto.
     * 
     * Muestra el viaje completo del producto desde su origen hasta la entrega,
     * incluyendo las emisiones de CO₂ por cada etapa.
     * 
     * @param productoId ID del producto
     * @return TrazabilidadProductoResponse con las etapas del viaje del producto
     */
    com.ecoshop.dto.Producto.TrazabilidadProductoResponse obtenerTrazabilidadProducto(Integer productoId);
}

