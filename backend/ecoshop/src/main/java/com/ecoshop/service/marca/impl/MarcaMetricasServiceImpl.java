package com.ecoshop.service.marca.impl;

import com.ecoshop.domain.Marca;
import com.ecoshop.domain.PedidoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.Marca.DashboardMarcaResponse;
import com.ecoshop.dto.Marca.DistribucionEcoBadgeResponse;
import com.ecoshop.dto.Marca.EstadisticaProductoResponse;
import com.ecoshop.dto.Marca.MetricasAmbientalesMarcaResponse;
import com.ecoshop.dto.Marca.MetricasVentasResponse;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.marca.MarcaRepository;
import com.ecoshop.repository.pedidoitem.PedidoItemRepository;
import com.ecoshop.repository.producto.ProductoRepository;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.marca.MarcaMetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para calcular métricas y estadísticas de marcas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarcaMetricasServiceImpl implements MarcaMetricasService {

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final ImpactoAmbientalService impactoAmbientalService;

    @Override
    public DashboardMarcaResponse obtenerDashboardMarca(Integer marcaId) {
        Marca marca = marcaRepository.findById(marcaId)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + marcaId));

        MetricasVentasResponse metricasVentas = calcularMetricasVentas(marcaId);
        MetricasAmbientalesMarcaResponse metricasAmbientales = calcularMetricasAmbientales(marcaId);
        List<EstadisticaProductoResponse> estadisticasProductos = calcularEstadisticasPorProducto(marcaId);

        return DashboardMarcaResponse.builder()
                .marcaId(marca.getMarcaId())
                .nombreOficial(marca.getNombreOficial())
                .metricasVentas(metricasVentas)
                .metricasAmbientales(metricasAmbientales)
                .estadisticasProductos(estadisticasProductos)
                .build();
    }

    @Override
    public MetricasVentasResponse obtenerMetricasVentas(Integer marcaId) {
        return calcularMetricasVentas(marcaId);
    }

    @Override
    public MetricasAmbientalesMarcaResponse obtenerMetricasAmbientales(Integer marcaId) {
        return calcularMetricasAmbientales(marcaId);
    }

    /**
     * Calcula las métricas de ventas de una marca.
     */
    private MetricasVentasResponse calcularMetricasVentas(Integer marcaId) {
        // Obtener todos los items de pedidos que contienen productos de la marca
        List<PedidoItem> items = pedidoItemRepository.findByProducto_Marca_MarcaId(marcaId);
        
        // Obtener todos los productos de la marca
        List<Producto> productos = productoRepository.findByMarca_MarcaId(marcaId);
        
        if (items.isEmpty()) {
            return MetricasVentasResponse.builder()
                    .totalVentas(BigDecimal.ZERO)
                    .totalPedidos(0)
                    .totalProductosVendidos(0)
                    .productosUnicosVendidos(0)
                    .promedioVentaPorPedido(BigDecimal.ZERO)
                    .totalProductosCatalogo(productos.size())
                    .build();
        }

        // Calcular total de ventas (suma de cantidad * precio_unitario)
        BigDecimal totalVentas = items.stream()
                .map(item -> item.getPrecioUnitario()
                        .multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtener pedidos únicos
        long totalPedidos = items.stream()
                .map(item -> item.getPedido().getPedidoId())
                .distinct()
                .count();

        // Total de productos vendidos (suma de cantidades)
        int totalProductosVendidos = items.stream()
                .mapToInt(PedidoItem::getCantidad)
                .sum();

        // Productos únicos vendidos
        long productosUnicosVendidos = items.stream()
                .map(item -> item.getProducto().getProductoId())
                .distinct()
                .count();

        // Promedio de venta por pedido
        BigDecimal promedioVentaPorPedido = totalPedidos > 0
                ? totalVentas.divide(new BigDecimal(totalPedidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MetricasVentasResponse.builder()
                .totalVentas(totalVentas)
                .totalPedidos((int) totalPedidos)
                .totalProductosVendidos(totalProductosVendidos)
                .productosUnicosVendidos((int) productosUnicosVendidos)
                .promedioVentaPorPedido(promedioVentaPorPedido)
                .totalProductosCatalogo(productos.size())
                .build();
    }

    /**
     * Calcula las métricas ambientales agregadas de una marca.
     */
    private MetricasAmbientalesMarcaResponse calcularMetricasAmbientales(Integer marcaId) {
        List<PedidoItem> items = pedidoItemRepository.findByProducto_Marca_MarcaId(marcaId);
        
        if (items.isEmpty()) {
            return MetricasAmbientalesMarcaResponse.builder()
                    .huellaCarbonoTotal(BigDecimal.ZERO)
                    .co2AhorradoTotal(BigDecimal.ZERO)
                    .aguaAhorradaTotal(0)
                    .promedioHuellaCarbonoPorProducto(BigDecimal.ZERO)
                    .distribucionEcoBadges(DistribucionEcoBadgeResponse.builder()
                            .bajoImpacto(0)
                            .medioImpacto(0)
                            .neutro(0)
                            .build())
                    .porcentajePromedioReciclable(BigDecimal.ZERO)
                    .build();
        }

        // Calcular huella de carbono total
        BigDecimal huellaCarbonoTotal = items.stream()
                .map(item -> {
                    BigDecimal huellaPorUnidad = impactoAmbientalService.calcularHuellaCarbonoProducto(item.getProducto());
                    return huellaPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular CO₂ ahorrado total
        BigDecimal co2AhorradoTotal = items.stream()
                .map(item -> {
                    BigDecimal co2AhorradoPorUnidad = impactoAmbientalService.calcularCO2Ahorrado(item.getProducto());
                    return co2AhorradoPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular agua ahorrada total (simplificado: asumiendo que cada producto ahorra agua)
        int aguaAhorradaTotal = items.stream()
                .mapToInt(item -> {
                    // Si el producto tiene consumoAgua, calcular ahorro
                    Integer consumoAgua = item.getProducto().getConsumoAgua();
                    if (consumoAgua != null && consumoAgua > 0) {
                        // Asumir que productos sostenibles ahorran 50% vs convencionales
                        return (consumoAgua / 2) * item.getCantidad();
                    }
                    return 0;
                })
                .sum();

        // Promedio de huella de carbono por producto vendido
        int totalUnidadesVendidas = items.stream()
                .mapToInt(PedidoItem::getCantidad)
                .sum();
        BigDecimal promedioHuellaCarbonoPorProducto = totalUnidadesVendidas > 0
                ? huellaCarbonoTotal.divide(new BigDecimal(totalUnidadesVendidas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Distribución de eco-badges
        Map<String, Long> distribucionEcoBadges = items.stream()
                .collect(Collectors.groupingBy(
                        item -> {
                            String ecoBadge = item.getProducto().getEcoBadge();
                            return ecoBadge != null ? ecoBadge : "neutro";
                        },
                        Collectors.counting()
                ));

        DistribucionEcoBadgeResponse distribucion = DistribucionEcoBadgeResponse.builder()
                .bajoImpacto(distribucionEcoBadges.getOrDefault("bajo_impacto", 0L).intValue())
                .medioImpacto(distribucionEcoBadges.getOrDefault("medio_impacto", 0L).intValue())
                .neutro(distribucionEcoBadges.getOrDefault("neutro", 0L).intValue())
                .build();

        // Porcentaje promedio de material reciclable
        BigDecimal porcentajePromedioReciclable = items.stream()
                .map(item -> {
                    Integer porcentaje = item.getProducto().getPorcentajeReciclable();
                    return porcentaje != null ? new BigDecimal(porcentaje) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(items.size()), 2, RoundingMode.HALF_UP);

        return MetricasAmbientalesMarcaResponse.builder()
                .huellaCarbonoTotal(huellaCarbonoTotal)
                .co2AhorradoTotal(co2AhorradoTotal)
                .aguaAhorradaTotal(aguaAhorradaTotal)
                .promedioHuellaCarbonoPorProducto(promedioHuellaCarbonoPorProducto)
                .distribucionEcoBadges(distribucion)
                .porcentajePromedioReciclable(porcentajePromedioReciclable)
                .build();
    }

    /**
     * Calcula estadísticas por producto de una marca.
     */
    private List<EstadisticaProductoResponse> calcularEstadisticasPorProducto(Integer marcaId) {
        List<Producto> productos = productoRepository.findByMarca_MarcaId(marcaId);
        
        return productos.stream()
                .map(producto -> {
                    List<PedidoItem> itemsProducto = pedidoItemRepository.findByProducto_ProductoId(producto.getProductoId());
                    
                    int unidadesVendidas = itemsProducto.stream()
                            .mapToInt(PedidoItem::getCantidad)
                            .sum();
                    
                    BigDecimal totalVentas = itemsProducto.stream()
                            .map(item -> item.getPrecioUnitario()
                                    .multiply(new BigDecimal(item.getCantidad())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    long numeroPedidos = itemsProducto.stream()
                            .map(item -> item.getPedido().getPedidoId())
                            .distinct()
                            .count();
                    
                    BigDecimal huellaCarbonoTotal = itemsProducto.stream()
                            .map(item -> {
                                BigDecimal huellaPorUnidad = impactoAmbientalService.calcularHuellaCarbonoProducto(producto);
                                return huellaPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    BigDecimal co2AhorradoTotal = itemsProducto.stream()
                            .map(item -> {
                                BigDecimal co2AhorradoPorUnidad = impactoAmbientalService.calcularCO2Ahorrado(producto);
                                return co2AhorradoPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    int aguaAhorradaTotal = itemsProducto.stream()
                            .mapToInt(item -> {
                                Integer consumoAgua = producto.getConsumoAgua();
                                if (consumoAgua != null && consumoAgua > 0) {
                                    return (consumoAgua / 2) * item.getCantidad();
                                }
                                return 0;
                            })
                            .sum();
                    
                    return EstadisticaProductoResponse.builder()
                            .productoId(producto.getProductoId())
                            .nombre(producto.getNombre())
                            .unidadesVendidas(unidadesVendidas)
                            .totalVentas(totalVentas)
                            .numeroPedidos((int) numeroPedidos)
                            .huellaCarbonoTotal(huellaCarbonoTotal)
                            .co2AhorradoTotal(co2AhorradoTotal)
                            .aguaAhorradaTotal(aguaAhorradaTotal)
                            .ecoBadge(producto.getEcoBadge())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

