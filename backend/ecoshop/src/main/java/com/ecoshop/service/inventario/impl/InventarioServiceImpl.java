package com.ecoshop.service.inventario.impl;

import com.ecoshop.domain.Producto;
import com.ecoshop.domain.StockHistorial;
import com.ecoshop.dto.Inventario.ActualizarStockRequestDTO;
import com.ecoshop.dto.Inventario.HistorialStockResponse;
import com.ecoshop.dto.Inventario.PrediccionDemandaResponse;
import com.ecoshop.dto.Inventario.StockBajoResponse;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.ProductoMapper;
import com.ecoshop.repository.producto.ProductoRepository;
import com.ecoshop.repository.stockhistorial.StockHistorialRepository;
import com.ecoshop.service.inventario.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de inventario avanzada.
 * 
 * Proporciona funcionalidades para:
 * - Alertas de stock bajo
 * - Historial de cambios de stock
 * - Predicción de demanda basada en ventas recientes
 * - Actualización de stock con registro automático
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final StockHistorialRepository stockHistorialRepository;
    private final ProductoMapper productoMapper;

    // Umbrales para niveles de alerta
    private static final int STOCK_CRITICO = 5;
    private static final int STOCK_BAJO = 10;
    private static final int STOCK_MEDIO = 20;

    @Override
    @Transactional(readOnly = true)
    public StockBajoResponse obtenerProductosStockBajo(Integer marcaId, Integer umbralStock) {
        int umbral = umbralStock != null ? umbralStock : STOCK_BAJO;

        // Buscar productos de la marca con stock bajo
        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getMarca() != null && p.getMarca().getMarcaId().equals(marcaId))
                .filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .filter(p -> p.getActivo() != null && p.getActivo())
                .sorted((p1, p2) -> Integer.compare(
                        p1.getStock() != null ? p1.getStock() : 0,
                        p2.getStock() != null ? p2.getStock() : 0))
                .collect(Collectors.toList());

        List<StockBajoResponse.ProductoStockBajo> productosStockBajo = productos.stream()
                .map(producto -> {
                    int stock = producto.getStock() != null ? producto.getStock() : 0;
                    String nivelAlerta = determinarNivelAlerta(stock);
                    String mensajeAlerta = generarMensajeAlerta(stock, nivelAlerta);
                    int stockMinimoRecomendado = calcularStockMinimoRecomendado(producto);

                    return StockBajoResponse.ProductoStockBajo.builder()
                            .productoId(producto.getProductoId())
                            .nombre(producto.getNombre())
                            .stockActual(stock)
                            .stockMinimoRecomendado(stockMinimoRecomendado)
                            .nivelAlerta(nivelAlerta)
                            .mensajeAlerta(mensajeAlerta)
                            .build();
                })
                .collect(Collectors.toList());

        String nombreMarca = productos.isEmpty() ? "N/A" : 
                            productos.get(0).getMarca().getNombreOficial();

        return StockBajoResponse.builder()
                .marcaId(marcaId)
                .nombreMarca(nombreMarca)
                .umbralStock(umbral)
                .totalProductosStockBajo(productosStockBajo.size())
                .productos(productosStockBajo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialStockResponse obtenerHistorialStock(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        List<StockHistorial> historial = stockHistorialRepository
                .findByProducto_ProductoIdOrderByFechaCambioDesc(productoId);

        List<HistorialStockResponse.RegistroStock> registros = historial.stream()
                .map(sh -> HistorialStockResponse.RegistroStock.builder()
                        .stockHistorialId(sh.getStockHistorialId())
                        .stockAnterior(sh.getStockAnterior())
                        .stockNuevo(sh.getStockNuevo())
                        .cantidadCambio(sh.getCantidadCambio())
                        .tipoMovimiento(sh.getTipoMovimiento())
                        .motivo(sh.getMotivo())
                        .usuarioId(sh.getUsuarioId())
                        .pedidoId(sh.getPedidoId())
                        .fechaCambio(sh.getFechaCambio())
                        .build())
                .collect(Collectors.toList());

        return HistorialStockResponse.builder()
                .productoId(productoId)
                .nombreProducto(producto.getNombre())
                .stockActual(producto.getStock() != null ? producto.getStock() : 0)
                .totalRegistros(registros.size())
                .registros(registros)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialStockResponse obtenerHistorialStockPorFecha(
            Integer productoId, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        // Si no se proporcionan fechas, usar los últimos 30 días
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now().minusDays(30);
        }
        if (fechaFin == null) {
            fechaFin = LocalDateTime.now();
        }

        List<StockHistorial> historial = stockHistorialRepository
                .findByProducto_ProductoIdAndFechaCambioBetween(productoId, fechaInicio, fechaFin);

        List<HistorialStockResponse.RegistroStock> registros = historial.stream()
                .map(sh -> HistorialStockResponse.RegistroStock.builder()
                        .stockHistorialId(sh.getStockHistorialId())
                        .stockAnterior(sh.getStockAnterior())
                        .stockNuevo(sh.getStockNuevo())
                        .cantidadCambio(sh.getCantidadCambio())
                        .tipoMovimiento(sh.getTipoMovimiento())
                        .motivo(sh.getMotivo())
                        .usuarioId(sh.getUsuarioId())
                        .pedidoId(sh.getPedidoId())
                        .fechaCambio(sh.getFechaCambio())
                        .build())
                .collect(Collectors.toList());

        return HistorialStockResponse.builder()
                .productoId(productoId)
                .nombreProducto(producto.getNombre())
                .stockActual(producto.getStock() != null ? producto.getStock() : 0)
                .totalRegistros(registros.size())
                .registros(registros)
                .build();
    }

    @Override
    @Transactional
    public com.ecoshop.dto.Producto.ProductoResponseDTO actualizarStock(
            Integer productoId, 
            ActualizarStockRequestDTO request, 
            Integer usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
        int stockNuevo = request.getNuevoStock();
        int cantidadCambio = stockNuevo - stockAnterior;

        // Validar tipo de movimiento
        if (!esTipoMovimientoValido(request.getTipoMovimiento())) {
            throw new BadRequestException("Tipo de movimiento no válido: " + request.getTipoMovimiento() + 
                    ". Valores válidos: entrada, salida, ajuste");
        }

        // Validar consistencia del cambio según el tipo de movimiento
        if ("entrada".equalsIgnoreCase(request.getTipoMovimiento()) && cantidadCambio <= 0) {
            throw new BadRequestException("Una entrada de stock debe aumentar el inventario");
        }
        if ("salida".equalsIgnoreCase(request.getTipoMovimiento()) && cantidadCambio >= 0) {
            throw new BadRequestException("Una salida de stock debe disminuir el inventario");
        }

        // Actualizar stock
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Registrar en historial
        String motivo = request.getMotivo() != null ? request.getMotivo() : 
                   "Actualización manual de stock";
        registrarCambioStock(productoId, stockAnterior, stockNuevo, 
                           request.getTipoMovimiento(), motivo, usuarioId, null);

        log.info("Stock actualizado para producto {}: {} -> {} (tipo: {}, usuario: {})", 
                productoId, stockAnterior, stockNuevo, request.getTipoMovimiento(), usuarioId);

        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrediccionDemandaResponse obtenerPrediccionDemanda(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace1Mes = ahora.minusMonths(1);
        LocalDateTime hace3Meses = ahora.minusMonths(3);

        // Obtener ventas del último mes
        Long ventasUltimoMes = stockHistorialRepository.contarVentasEnPeriodo(
                productoId, hace1Mes, ahora);

        // Obtener ventas de los últimos 3 meses
        Long ventasUltimos3Meses = stockHistorialRepository.contarVentasEnPeriodo(
                productoId, hace3Meses, ahora);

        // Calcular promedios
        BigDecimal promedioVentasMensual = BigDecimal.valueOf(ventasUltimos3Meses)
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        BigDecimal promedioVentasSemanal = promedioVentasMensual
                .divide(BigDecimal.valueOf(4.33), 2, RoundingMode.HALF_UP); // Promedio de semanas por mes

        // Calcular días hasta agotamiento
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        int diasHastaAgotamiento = 0;
        if (promedioVentasSemanal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ventasDiarias = promedioVentasSemanal.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
            if (ventasDiarias.compareTo(BigDecimal.ZERO) > 0) {
                diasHastaAgotamiento = BigDecimal.valueOf(stockActual)
                        .divide(ventasDiarias, 0, RoundingMode.HALF_UP)
                        .intValue();
            }
        }

        // Calcular stock mínimo recomendado (2 semanas de ventas promedio)
        int stockMinimoRecomendado = promedioVentasSemanal
                .multiply(BigDecimal.valueOf(2))
                .intValue();

        // Generar recomendación
        String recomendacion = generarRecomendacion(stockActual, stockMinimoRecomendado, diasHastaAgotamiento);
        int cantidadRecomendadaReposicion = Math.max(0, stockMinimoRecomendado - stockActual);

        PrediccionDemandaResponse.AnalisisDemanda analisis = PrediccionDemandaResponse.AnalisisDemanda.builder()
                .ventasUltimoMes(ventasUltimoMes.intValue())
                .ventasUltimos3Meses(ventasUltimos3Meses.intValue())
                .promedioVentasMensual(promedioVentasMensual)
                .promedioVentasSemanal(promedioVentasSemanal)
                .diasHastaAgotamiento(diasHastaAgotamiento)
                .recomendacion(recomendacion)
                .cantidadRecomendadaReposicion(cantidadRecomendadaReposicion)
                .build();

        return PrediccionDemandaResponse.builder()
                .productoId(productoId)
                .nombreProducto(producto.getNombre())
                .stockActual(stockActual)
                .stockMinimoRecomendado(stockMinimoRecomendado)
                .analisis(analisis)
                .build();
    }

    @Override
    @Transactional
    public void registrarCambioStock(
            Integer productoId,
            Integer stockAnterior,
            Integer stockNuevo,
            String tipoMovimiento,
            String motivo,
            Integer usuarioId,
            Integer pedidoId) {
        
        int cantidadCambio = stockNuevo - stockAnterior;

        StockHistorial historial = StockHistorial.builder()
                .producto(productoRepository.findById(productoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId)))
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .cantidadCambio(cantidadCambio)
                .tipoMovimiento(tipoMovimiento)
                .motivo(motivo)
                .usuarioId(usuarioId)
                .pedidoId(pedidoId)
                .build();

        stockHistorialRepository.save(historial);
        log.debug("Registro de stock guardado: producto={}, cambio={}, tipo={}", 
                productoId, cantidadCambio, tipoMovimiento);
    }

    /**
     * Determina el nivel de alerta según el stock actual.
     */
    private String determinarNivelAlerta(int stock) {
        if (stock <= STOCK_CRITICO) {
            return "critico";
        } else if (stock <= STOCK_BAJO) {
            return "bajo";
        } else if (stock <= STOCK_MEDIO) {
            return "medio";
        } else {
            return "suficiente";
        }
    }

    /**
     * Genera un mensaje de alerta según el nivel de stock.
     */
    private String generarMensajeAlerta(int stock, String nivelAlerta) {
        return switch (nivelAlerta) {
            case "critico" -> "⚠️ Stock crítico: Reposición urgente necesaria";
            case "bajo" -> "⚠️ Stock bajo: Considerar reposición pronto";
            case "medio" -> "ℹ️ Stock medio: Monitorear ventas";
            default -> "✓ Stock suficiente";
        };
    }

    /**
     * Calcula el stock mínimo recomendado basado en ventas recientes.
     */
    private int calcularStockMinimoRecomendado(Producto producto) {
        LocalDateTime hace3Meses = LocalDateTime.now().minusMonths(3);
        Long ventas = stockHistorialRepository.contarVentasEnPeriodo(
                producto.getProductoId(), hace3Meses, LocalDateTime.now());

        if (ventas > 0) {
            // Stock mínimo = 2 semanas de ventas promedio
            BigDecimal promedioSemanal = BigDecimal.valueOf(ventas)
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP); // 12 semanas en 3 meses
            return promedioSemanal.multiply(BigDecimal.valueOf(2)).intValue();
        }
        return 10; // Valor por defecto si no hay historial de ventas
    }

    /**
     * Valida si el tipo de movimiento es válido.
     */
    private boolean esTipoMovimientoValido(String tipoMovimiento) {
        return tipoMovimiento != null && (
                "entrada".equalsIgnoreCase(tipoMovimiento) ||
                "salida".equalsIgnoreCase(tipoMovimiento) ||
                "ajuste".equalsIgnoreCase(tipoMovimiento) ||
                "venta".equalsIgnoreCase(tipoMovimiento) ||
                "cancelacion".equalsIgnoreCase(tipoMovimiento)
        );
    }

    /**
     * Genera una recomendación basada en el análisis de demanda.
     */
    private String generarRecomendacion(int stockActual, int stockMinimoRecomendado, int diasHastaAgotamiento) {
        if (stockActual < stockMinimoRecomendado) {
            return "reponer";
        } else if (diasHastaAgotamiento < 14) {
            return "monitorear";
        } else {
            return "suficiente";
        }
    }
}

