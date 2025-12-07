package com.ecoshop.service.impacto.impl;

import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.PedidoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoPedidoResponse;
import com.ecoshop.dto.ImpactoAmbiental.CalculoImpactoProductoResponse;
import com.ecoshop.dto.ImpactoAmbiental.ComparacionProductosResponse;
import com.ecoshop.dto.ImpactoAmbiental.MetricasAmbientalesResponse;
import com.ecoshop.dto.Usuario.DashboardImpactoResponse;
import com.ecoshop.dto.Usuario.HistorialImpactoResponse;
import com.ecoshop.dto.Usuario.MetricasAmbientalesUsuarioResponse;
import com.ecoshop.dto.Usuario.TendenciasImpactoResponse;
import com.ecoshop.enums.EcoBadge;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedidoitem.PedidoItemRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.repository.producto.ProductoRepository;
import com.ecoshop.service.usuario.EcoPuntosService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de impacto ambiental.
 * 
 * Calcula la huella de carbono considerando:
 * - Emisiones por fabricación
 * - Emisiones por materiales
 * - Emisiones por transporte (distancia desde origen)
 * - Emisiones por empaque
 * - Emisiones por entrega (última milla)
 */
@Service
@Transactional(readOnly = true)
public class ImpactoAmbientalServiceImpl implements ImpactoAmbientalService {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final EcoPuntosService ecoPuntosService;

    public ImpactoAmbientalServiceImpl(
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            PedidoItemRepository pedidoItemRepository,
            UsuarioRepository usuarioRepository,
            @Lazy EcoPuntosService ecoPuntosService) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.ecoPuntosService = ecoPuntosService;
    }

    // Factores de emisión (kg CO₂ por unidad)
    private static final BigDecimal EMISION_FABRICACION_BASE = new BigDecimal("0.5"); // Base por producto
    private static final BigDecimal EMISION_MATERIAL_RECICLADO = new BigDecimal("0.2"); // Reducción si es reciclado
    private static final BigDecimal EMISION_MATERIAL_CONVENCIONAL = new BigDecimal("0.8"); // Material convencional
    private static final BigDecimal EMISION_TRANSPORTE_POR_KM = new BigDecimal("0.0001"); // Por km de distancia
    private static final BigDecimal EMISION_EMPAQUE_BASE = new BigDecimal("0.1"); // Empaque base
    private static final BigDecimal EMISION_EMPAQUE_RECICLABLE = new BigDecimal("0.05"); // Empaque reciclable
    private static final BigDecimal EMISION_ENTREGA_ULTIMA_MILLA = new BigDecimal("0.2"); // Entrega última milla
    
    // Factor de reducción para productos sostenibles vs convencionales
    private static final BigDecimal FACTOR_AHORRO_SOSTENIBLE = new BigDecimal("0.3"); // 30% menos emisiones
    
    // Factores para cálculo de consumo de agua (litros por producto)
    private static final Integer CONSUMO_AGUA_BASE = 100; // Base por producto
    private static final BigDecimal FACTOR_REDUCCION_AGUA_RECICLADO = new BigDecimal("0.5"); // 50% menos agua si es reciclado

    @Override
    public BigDecimal calcularHuellaCarbonoProducto(Producto producto) {
        if (producto == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal emisionesFabricacion = calcularEmisionesFabricacion(producto);
        BigDecimal emisionesMateriales = calcularEmisionesMateriales(producto);
        BigDecimal emisionesTransporte = calcularEmisionesTransporte(producto);
        BigDecimal emisionesEmpaque = calcularEmisionesEmpaque(producto);

        return emisionesFabricacion
                .add(emisionesMateriales)
                .add(emisionesTransporte)
                .add(emisionesEmpaque)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularHuellaCarbonoPedido(Pedido pedido) {
        if (pedido == null) {
            return BigDecimal.ZERO;
        }

        // Obtener items del pedido
        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());
        
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Calcular emisiones de productos
        BigDecimal emisionesProductos = items.stream()
                .map(item -> {
                    BigDecimal huellaPorUnidad = calcularHuellaCarbonoProducto(item.getProducto());
                    return huellaPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Emisiones de transporte y entrega
        BigDecimal emisionesTransporte = EMISION_ENTREGA_ULTIMA_MILLA;
        
        // Si hay múltiples productos, agregar emisión adicional por consolidación
        if (items.size() > 1) {
            emisionesTransporte = emisionesTransporte.add(new BigDecimal("0.1"));
        }

        return emisionesProductos
                .add(emisionesTransporte)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public CalculoImpactoProductoResponse calcularImpactoProducto(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        BigDecimal emisionesFabricacion = calcularEmisionesFabricacion(producto);
        BigDecimal emisionesMateriales = calcularEmisionesMateriales(producto);
        BigDecimal emisionesTransporte = calcularEmisionesTransporte(producto);
        BigDecimal emisionesEmpaque = calcularEmisionesEmpaque(producto);
        BigDecimal huellaTotal = emisionesFabricacion
                .add(emisionesMateriales)
                .add(emisionesTransporte)
                .add(emisionesEmpaque)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal co2Ahorrado = calcularCO2Ahorrado(producto);
        
        // Calcular eco-badge automáticamente
        EcoBadge ecoBadgeCalculado = calcularEcoBadge(producto);

        return CalculoImpactoProductoResponse.builder()
                .productoId(producto.getProductoId())
                .nombreProducto(producto.getNombre())
                .emisionesFabricacion(emisionesFabricacion)
                .emisionesMateriales(emisionesMateriales)
                .emisionesTransporte(emisionesTransporte)
                .emisionesEmpaque(emisionesEmpaque)
                .huellaCarbonoTotal(huellaTotal)
                .co2AhorradoVsConvencional(co2Ahorrado)
                .origen(producto.getOrigen())
                .porcentajeReciclable(producto.getPorcentajeReciclable())
                .ecoBadge(ecoBadgeCalculado != null ? ecoBadgeCalculado.getValor() : null)
                .build();
    }

    @Override
    public CalculoImpactoPedidoResponse calcularImpactoPedido(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedidoId);
        
        List<CalculoImpactoPedidoResponse.ItemPedidoImpacto> itemsImpacto = items.stream()
                .map(item -> {
                    BigDecimal huellaPorUnidad = calcularHuellaCarbonoProducto(item.getProducto());
                    BigDecimal huellaTotal = huellaPorUnidad.multiply(new BigDecimal(item.getCantidad()));
                    
                    return CalculoImpactoPedidoResponse.ItemPedidoImpacto.builder()
                            .productoId(item.getProducto().getProductoId())
                            .nombreProducto(item.getProducto().getNombre())
                            .cantidad(item.getCantidad())
                            .huellaCarbonoPorUnidad(huellaPorUnidad)
                            .huellaCarbonoTotal(huellaTotal.setScale(2, RoundingMode.HALF_UP))
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal emisionesProductos = itemsImpacto.stream()
                .map(CalculoImpactoPedidoResponse.ItemPedidoImpacto::getHuellaCarbonoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal emisionesTransporte = EMISION_ENTREGA_ULTIMA_MILLA;
        if (items.size() > 1) {
            emisionesTransporte = emisionesTransporte.add(new BigDecimal("0.1"));
        }
        
        BigDecimal emisionesEntrega = EMISION_ENTREGA_ULTIMA_MILLA;
        
        BigDecimal huellaTotal = emisionesProductos
                .add(emisionesTransporte)
                .add(emisionesEntrega)
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular CO₂ ahorrado total
        BigDecimal co2AhorradoTotal = items.stream()
                .map(item -> calcularCO2Ahorrado(item.getProducto())
                        .multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return CalculoImpactoPedidoResponse.builder()
                .pedidoId(pedido.getPedidoId())
                .emisionesProductos(emisionesProductos)
                .emisionesTransporte(emisionesTransporte)
                .emisionesEntrega(emisionesEntrega)
                .huellaCarbonoTotal(huellaTotal)
                .co2AhorradoTotal(co2AhorradoTotal)
                .items(itemsImpacto)
                .build();
    }

    @Override
    public ComparacionProductosResponse compararProductos(List<Integer> productoIds) {
        if (productoIds == null || productoIds.isEmpty() || productoIds.size() > 4) {
            throw new IllegalArgumentException("Debe proporcionar entre 1 y 4 productos para comparar");
        }

        List<Producto> productos = productoRepository.findAllById(productoIds);
        
        if (productos.size() != productoIds.size()) {
            throw new ResourceNotFoundException("Uno o más productos no fueron encontrados");
        }

        List<ComparacionProductosResponse.ProductoComparacion> comparaciones = productos.stream()
                .map(producto -> {
                    BigDecimal huella = calcularHuellaCarbonoProducto(producto);
                    EcoBadge ecoBadge = calcularEcoBadge(producto);
                    return ComparacionProductosResponse.ProductoComparacion.builder()
                            .productoId(producto.getProductoId())
                            .nombre(producto.getNombre())
                            .precio(producto.getPrecio())
                            .huellaCarbono(huella)
                            .ecoBadge(ecoBadge != null ? ecoBadge.getValor() : null)
                            .origen(producto.getOrigen())
                            .porcentajeReciclable(producto.getPorcentajeReciclable())
                            .materiales(producto.getMateriales())
                            .build();
                })
                .collect(Collectors.toList());

        // Encontrar producto con menor impacto
        Integer productoMenorImpacto = comparaciones.stream()
                .min(Comparator.comparing(ComparacionProductosResponse.ProductoComparacion::getHuellaCarbono))
                .map(ComparacionProductosResponse.ProductoComparacion::getProductoId)
                .orElse(null);

        // Encontrar producto con menor precio
        Integer productoMenorPrecio = comparaciones.stream()
                .min(Comparator.comparing(ComparacionProductosResponse.ProductoComparacion::getPrecio))
                .map(ComparacionProductosResponse.ProductoComparacion::getProductoId)
                .orElse(null);

        // Producto recomendado: balance entre precio e impacto
        Integer productoRecomendado = comparaciones.stream()
                .min(Comparator.comparing(p -> {
                    // Normalizar precio e impacto para comparar
                    BigDecimal precioNormalizado = p.getPrecio().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal impactoNormalizado = p.getHuellaCarbono().multiply(new BigDecimal("10"));
                    return precioNormalizado.add(impactoNormalizado);
                }))
                .map(ComparacionProductosResponse.ProductoComparacion::getProductoId)
                .orElse(null);

        return ComparacionProductosResponse.builder()
                .productos(comparaciones)
                .productoMenorImpacto(productoMenorImpacto)
                .productoMenorPrecio(productoMenorPrecio)
                .productoRecomendado(productoRecomendado)
                .build();
    }

    @Override
    public BigDecimal calcularCO2Ahorrado(Producto producto) {
        if (producto == null) {
            return BigDecimal.ZERO;
        }

        // Calcular huella del producto actual
        BigDecimal huellaActual = calcularHuellaCarbonoProducto(producto);
        
        // Calcular huella estimada de un producto convencional equivalente
        BigDecimal huellaConvencional = huellaActual.divide(
                BigDecimal.ONE.subtract(FACTOR_AHORRO_SOSTENIBLE), 
                2, 
                RoundingMode.HALF_UP
        );

        // CO₂ ahorrado = diferencia
        BigDecimal co2Ahorrado = huellaConvencional.subtract(huellaActual);
        
        return co2Ahorrado.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public EcoBadge calcularEcoBadge(Producto producto) {
        if (producto == null) {
            return EcoBadge.NEUTRO;
        }

        // Calcular puntuación total (0-100)
        int puntuacion = 0;

        // 1. Factor: Huella de carbono (0-30 puntos)
        // Menor huella = más puntos
        BigDecimal huellaCarbono = calcularHuellaCarbonoProducto(producto);
        if (huellaCarbono.compareTo(new BigDecimal("0.5")) <= 0) {
            puntuacion += 30; // Muy baja huella
        } else if (huellaCarbono.compareTo(new BigDecimal("1.0")) <= 0) {
            puntuacion += 20; // Baja huella
        } else if (huellaCarbono.compareTo(new BigDecimal("2.0")) <= 0) {
            puntuacion += 10; // Huella moderada
        } else {
            puntuacion += 0; // Alta huella
        }

        // 2. Factor: Porcentaje de material reciclable (0-25 puntos)
        // Mayor porcentaje = más puntos
        if (producto.getPorcentajeReciclable() != null) {
            int porcentaje = producto.getPorcentajeReciclable();
            if (porcentaje >= 90) {
                puntuacion += 25; // Muy reciclable
            } else if (porcentaje >= 70) {
                puntuacion += 18; // Muy reciclable
            } else if (porcentaje >= 50) {
                puntuacion += 12; // Moderadamente reciclable
            } else if (porcentaje >= 30) {
                puntuacion += 6; // Poco reciclable
            } else {
                puntuacion += 0; // No reciclable
            }
        }

        // 3. Factor: Distancia de transporte (0-25 puntos)
        // Menor distancia = más puntos
        BigDecimal distancia = estimarDistanciaDesdeOrigen(producto.getOrigen());
        if (distancia.compareTo(new BigDecimal("100")) <= 0) {
            puntuacion += 25; // Local o muy cercano
        } else if (distancia.compareTo(new BigDecimal("1000")) <= 0) {
            puntuacion += 18; // Regional
        } else if (distancia.compareTo(new BigDecimal("5000")) <= 0) {
            puntuacion += 10; // Nacional/continental
        } else {
            puntuacion += 0; // Internacional distante
        }

        // 4. Factor: Certificaciones ambientales (0-20 puntos)
        // Más certificaciones = más puntos
        if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
            int numCertificaciones = producto.getCertificaciones().size();
            if (numCertificaciones >= 3) {
                puntuacion += 20; // Múltiples certificaciones
            } else if (numCertificaciones == 2) {
                puntuacion += 13; // Dos certificaciones
            } else {
                puntuacion += 7; // Una certificación
            }
        }

        // Determinar el badge según la puntuación total
        if (puntuacion >= 70) {
            return EcoBadge.BAJO_IMPACTO; // 🌱 Bajo impacto (Verde)
        } else if (puntuacion >= 40) {
            return EcoBadge.MEDIO_IMPACTO; // 🟠 Medio impacto (Naranja)
        } else {
            return EcoBadge.NEUTRO; // 🔵 Neutro (Azul)
        }
    }

    /**
     * Calcula las emisiones por fabricación del producto.
     */
    private BigDecimal calcularEmisionesFabricacion(Producto producto) {
        // Base de fabricación
        BigDecimal base = EMISION_FABRICACION_BASE;
        
        // Si tiene certificaciones, reducir emisiones
        if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
            // Cada certificación reduce un 5% las emisiones
            BigDecimal factorReduccion = BigDecimal.ONE.subtract(
                    new BigDecimal(producto.getCertificaciones().size())
                            .multiply(new BigDecimal("0.05"))
            );
            base = base.multiply(factorReduccion.max(new BigDecimal("0.7"))); // Máximo 30% de reducción
        }
        
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula las emisiones por materiales utilizados.
     */
    private BigDecimal calcularEmisionesMateriales(Producto producto) {
        if (producto.getPorcentajeReciclable() == null) {
            return EMISION_MATERIAL_CONVENCIONAL;
        }

        // Si es 100% reciclable, usar emisión reducida
        if (producto.getPorcentajeReciclable() >= 100) {
            return EMISION_MATERIAL_RECICLADO;
        }

        // Calcular proporción entre reciclado y convencional
        BigDecimal porcentajeReciclado = new BigDecimal(producto.getPorcentajeReciclable())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal porcentajeConvencional = BigDecimal.ONE.subtract(porcentajeReciclado);

        return EMISION_MATERIAL_RECICLADO.multiply(porcentajeReciclado)
                .add(EMISION_MATERIAL_CONVENCIONAL.multiply(porcentajeConvencional))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula las emisiones por transporte desde el origen.
     */
    private BigDecimal calcularEmisionesTransporte(Producto producto) {
        // Si no tiene origen, usar distancia promedio
        if (producto.getOrigen() == null || producto.getOrigen().trim().isEmpty()) {
            return EMISION_TRANSPORTE_POR_KM.multiply(new BigDecimal("1000")); // 1000 km por defecto
        }

        // Calcular distancia estimada según el origen
        BigDecimal distancia = estimarDistanciaDesdeOrigen(producto.getOrigen());
        
        return EMISION_TRANSPORTE_POR_KM.multiply(distancia)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula las emisiones por empaque.
     */
    private BigDecimal calcularEmisionesEmpaque(Producto producto) {
        // Si el producto es reciclable, el empaque también lo es probablemente
        if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 50) {
            return EMISION_EMPAQUE_RECICLABLE;
        }
        
        return EMISION_EMPAQUE_BASE;
    }

    /**
     * Estima la distancia desde el origen del producto.
     * En una implementación real, esto podría usar una API de geolocalización.
     */
    private BigDecimal estimarDistanciaDesdeOrigen(String origen) {
        // Distancias estimadas desde un punto central (ej: Santiago, Chile)
        // En producción, usar una API de geocodificación y cálculo de distancias
        
        String origenLower = origen.toLowerCase();
        
        if (origenLower.contains("local") || origenLower.contains("chile") || origenLower.contains("santiago")) {
            return new BigDecimal("50"); // Local
        } else if (origenLower.contains("argentina") || origenLower.contains("buenos aires")) {
            return new BigDecimal("1400"); // Argentina
        } else if (origenLower.contains("peru") || origenLower.contains("lima")) {
            return new BigDecimal("2400"); // Perú
        } else if (origenLower.contains("colombia") || origenLower.contains("bogota")) {
            return new BigDecimal("4200"); // Colombia
        } else if (origenLower.contains("mexico") || origenLower.contains("ciudad de mexico")) {
            return new BigDecimal("6000"); // México
        } else if (origenLower.contains("españa") || origenLower.contains("madrid")) {
            return new BigDecimal("11000"); // España
        } else if (origenLower.contains("china") || origenLower.contains("beijing")) {
            return new BigDecimal("19000"); // China
        } else if (origenLower.contains("australia") || origenLower.contains("sydney")) {
            return new BigDecimal("12000"); // Australia
        } else {
            return new BigDecimal("5000"); // Distancia promedio internacional
        }
    }

    @Override
    @Transactional
    public Producto calcularYAlmacenarMetricasDetalladas(Producto producto) {
        if (producto == null) {
            return null;
        }

        // Calcular todas las emisiones
        BigDecimal emisionesFabricacion = calcularEmisionesFabricacion(producto);
        BigDecimal emisionesMateriales = calcularEmisionesMateriales(producto);
        BigDecimal emisionesTransporte = calcularEmisionesTransporte(producto);
        BigDecimal emisionesEmpaque = calcularEmisionesEmpaque(producto);
        BigDecimal emisionesEntrega = EMISION_ENTREGA_ULTIMA_MILLA;
        
        // Calcular total
        BigDecimal huellaTotal = emisionesFabricacion
                .add(emisionesMateriales)
                .add(emisionesTransporte)
                .add(emisionesEmpaque)
                .add(emisionesEntrega)
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular CO₂ ahorrado
        BigDecimal co2Ahorrado = calcularCO2Ahorrado(producto);

        // Calcular consumo de agua
        Integer consumoAgua = calcularConsumoAgua(producto);

        // Calcular distancia de transporte
        Integer distanciaTransporte = estimarDistanciaDesdeOrigen(producto.getOrigen()).intValue();

        // Almacenar todas las métricas en el producto
        producto.setEmisionesFabricacion(emisionesFabricacion);
        producto.setEmisionesTransporte(emisionesTransporte);
        producto.setEmisionesEmpaque(emisionesEmpaque);
        producto.setEmisionesEntrega(emisionesEntrega);
        producto.setHuellaCarbonoTotal(huellaTotal);
        producto.setCo2AhorradoVsConvencional(co2Ahorrado);
        producto.setConsumoAgua(consumoAgua);
        producto.setDistanciaTransporte(distanciaTransporte);

        return producto;
    }

    @Override
    public MetricasAmbientalesResponse obtenerMetricasAmbientales(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        // Asegurar que las métricas estén calculadas
        if (producto.getEmisionesFabricacion() == null) {
            producto = calcularYAlmacenarMetricasDetalladas(producto);
            productoRepository.save(producto);
        }

        // Calcular emisiones de materiales (no se almacenan, se calculan)
        BigDecimal emisionesMateriales = calcularEmisionesMateriales(producto);

        // Calcular equivalencias
        String equivalenteAgua = calcularEquivalenteAgua(producto.getConsumoAgua());
        String equivalenteCO2 = calcularEquivalenteCO2(producto.getHuellaCarbonoTotal());

        return MetricasAmbientalesResponse.builder()
                .productoId(producto.getProductoId())
                .nombreProducto(producto.getNombre())
                .emisionesFabricacion(producto.getEmisionesFabricacion())
                .emisionesMateriales(emisionesMateriales)
                .emisionesTransporte(producto.getEmisionesTransporte())
                .emisionesEmpaque(producto.getEmisionesEmpaque())
                .emisionesEntrega(producto.getEmisionesEntrega())
                .huellaCarbonoTotal(producto.getHuellaCarbonoTotal())
                .co2AhorradoVsConvencional(producto.getCo2AhorradoVsConvencional())
                .consumoAgua(producto.getConsumoAgua())
                .distanciaTransporte(producto.getDistanciaTransporte())
                .porcentajeReciclable(producto.getPorcentajeReciclable())
                .ecoBadge(producto.getEcoBadge())
                .origen(producto.getOrigen())
                .materiales(producto.getMateriales())
                .equivalenteAgua(equivalenteAgua)
                .equivalenteCO2(equivalenteCO2)
                .build();
    }

    /**
     * Calcula el consumo de agua estimado para un producto.
     */
    private Integer calcularConsumoAgua(Producto producto) {
        Integer consumoBase = CONSUMO_AGUA_BASE;

        // Si el producto es reciclable, reduce el consumo de agua
        if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 50) {
            consumoBase = consumoBase - (int)(consumoBase * FACTOR_REDUCCION_AGUA_RECICLADO.doubleValue());
        }

        // Si tiene certificaciones, reduce aún más
        if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
            int reduccionPorCertificacion = consumoBase / 10; // 10% por certificación
            consumoBase = Math.max(consumoBase - (producto.getCertificaciones().size() * reduccionPorCertificacion), consumoBase / 2);
        }

        return consumoBase;
    }

    /**
     * Calcula el equivalente de agua en términos comprensibles.
     */
    private String calcularEquivalenteAgua(Integer litros) {
        if (litros == null || litros == 0) {
            return "Sin consumo de agua registrado";
        }

        // Una ducha promedio usa ~150 litros
        int duchas = litros / 150;
        if (duchas > 0) {
            return String.format("Equivalente a %d ducha%s de 5 minutos", duchas, duchas > 1 ? "s" : "");
        }

        // Botellas de agua de 500ml
        int botellas = litros * 2;
        return String.format("Equivalente a %d botella%s de agua (500ml)", botellas, botellas > 1 ? "s" : "");
    }

    /**
     * Calcula el equivalente de CO₂ en términos comprensibles.
     */
    private String calcularEquivalenteCO2(BigDecimal kgCO2) {
        if (kgCO2 == null || kgCO2.compareTo(BigDecimal.ZERO) == 0) {
            return "Sin emisiones";
        }

        // Un auto promedio emite ~120g CO₂ por km
        // 1 kg CO₂ = 1000g, entonces 1 kg = ~8.3 km
        BigDecimal kmEnAuto = kgCO2.multiply(new BigDecimal("8.3"));
        
        if (kmEnAuto.compareTo(new BigDecimal("1")) < 0) {
            return String.format("Equivalente a %.1f metros en auto", kmEnAuto.multiply(new BigDecimal("1000")).doubleValue());
        } else if (kmEnAuto.compareTo(new BigDecimal("10")) < 0) {
            return String.format("Equivalente a %.1f km en auto", kmEnAuto.doubleValue());
        } else {
            return String.format("Equivalente a %.0f km en auto", kmEnAuto.doubleValue());
        }
    }

    @Override
    public DashboardImpactoResponse obtenerDashboardImpacto(Integer usuarioId) {
        // Validar que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
        }

        // Obtener todos los pedidos entregados del usuario (excluir cancelados)
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(usuarioId)
                .stream()
                .filter(p -> !"cancelado".equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.toList());

        // Calcular métricas agregadas
        BigDecimal co2AhorradoTotal = BigDecimal.ZERO;
        Integer aguaAhorradaTotal = 0;
        Integer comprasSostenibles = pedidos.size();
        BigDecimal materialRecicladoTotal = BigDecimal.ZERO;

        for (Pedido pedido : pedidos) {
            List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());
            
            for (PedidoItem item : items) {
                Producto producto = item.getProducto();
                Integer cantidad = item.getCantidad();
                
                // CO₂ ahorrado
                if (producto.getCo2AhorradoVsConvencional() != null) {
                    BigDecimal co2AhorradoItem = producto.getCo2AhorradoVsConvencional()
                            .multiply(new BigDecimal(cantidad));
                    co2AhorradoTotal = co2AhorradoTotal.add(co2AhorradoItem);
                }
                
                // Agua ahorrada (estimación: productos sostenibles usan 50% menos agua)
                if (producto.getConsumoAgua() != null) {
                    // Agua ahorrada = agua que se habría usado en producto convencional - agua usada
                    Integer aguaConvencional = producto.getConsumoAgua() * 2; // Producto convencional usa el doble
                    Integer aguaAhorradaItem = (aguaConvencional - producto.getConsumoAgua()) * cantidad;
                    aguaAhorradaTotal += aguaAhorradaItem;
                }
                
                // Material reciclado (estimación basada en porcentaje reciclable)
                if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() > 0) {
                    // Asumir peso promedio de 0.5 kg por producto
                    BigDecimal pesoProducto = new BigDecimal("0.5");
                    BigDecimal porcentajeReciclado = new BigDecimal(producto.getPorcentajeReciclable())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal materialRecicladoItem = pesoProducto
                            .multiply(porcentajeReciclado)
                            .multiply(new BigDecimal(cantidad));
                    materialRecicladoTotal = materialRecicladoTotal.add(materialRecicladoItem);
                }
            }
        }

        // Calcular equivalencias
        String co2Equivalente = calcularEquivalenteCO2Ahorrado(co2AhorradoTotal);
        String aguaEquivalente = calcularEquivalenteAguaAhorrada(aguaAhorradaTotal);

        // Obtener eco-puntos del usuario (usar el servicio de eco-puntos)
        Integer ecoPuntos = 0;
        String nivelEcoPuntos = "Bronce";
        Integer ecoPuntosMeta = 500;
        
        try {
            com.ecoshop.dto.EcoPuntos.EcoPuntosResponse ecoPuntosResponse = ecoPuntosService.obtenerEcoPuntos(usuarioId);
            ecoPuntos = ecoPuntosResponse.getEcoPuntos();
            nivelEcoPuntos = ecoPuntosResponse.getNivelEcoPuntos();
            ecoPuntosMeta = ecoPuntosResponse.getMetaEcoPuntos();
        } catch (Exception e) {
            // Si hay error, usar cálculo básico como fallback
            ecoPuntos = calcularEcoPuntosBasico(co2AhorradoTotal, comprasSostenibles);
            nivelEcoPuntos = calcularNivelEcoPuntos(ecoPuntos);
        }

        // Objetivos por defecto (en el futuro se pueden almacenar en la entidad Usuario)
        BigDecimal co2Meta = new BigDecimal("50.0");

        return DashboardImpactoResponse.builder()
                .co2AhorradoTotal(co2AhorradoTotal.setScale(2, RoundingMode.HALF_UP))
                .co2AhorradoEquivalente(co2Equivalente)
                .aguaAhorradaTotal(aguaAhorradaTotal)
                .aguaAhorradaEquivalente(aguaEquivalente)
                .comprasSostenibles(comprasSostenibles)
                .materialRecicladoTotal(materialRecicladoTotal.setScale(2, RoundingMode.HALF_UP))
                .ecoPuntos(ecoPuntos)
                .nivelEcoPuntos(nivelEcoPuntos)
                .objetivos(DashboardImpactoResponse.ObjetivosSostenibilidad.builder()
                        .co2Meta(co2Meta)
                        .co2Progreso(co2AhorradoTotal.setScale(2, RoundingMode.HALF_UP))
                        .ecoPuntosMeta(ecoPuntosMeta)
                        .ecoPuntosProgreso(ecoPuntos)
                        .build())
                .build();
    }

    @Override
    public MetricasAmbientalesUsuarioResponse obtenerMetricasAmbientalesUsuario(Integer usuarioId) {
        DashboardImpactoResponse dashboard = obtenerDashboardImpacto(usuarioId);
        
        return MetricasAmbientalesUsuarioResponse.builder()
                .co2AhorradoTotal(dashboard.getCo2AhorradoTotal())
                .aguaAhorradaTotal(dashboard.getAguaAhorradaTotal())
                .comprasSostenibles(dashboard.getComprasSostenibles())
                .materialRecicladoTotal(dashboard.getMaterialRecicladoTotal())
                .ecoPuntos(dashboard.getEcoPuntos())
                .nivelEcoPuntos(dashboard.getNivelEcoPuntos())
                .build();
    }

    /**
     * Calcula el equivalente de CO₂ ahorrado en términos comprensibles.
     */
    private String calcularEquivalenteCO2Ahorrado(BigDecimal kgCO2) {
        if (kgCO2 == null || kgCO2.compareTo(BigDecimal.ZERO) == 0) {
            return "Sin CO₂ ahorrado aún";
        }

        // Un árbol absorbe aproximadamente 22 kg de CO₂ al año
        BigDecimal arboles = kgCO2.divide(new BigDecimal("22"), 1, RoundingMode.HALF_UP);
        
        if (arboles.compareTo(new BigDecimal("1")) < 0) {
            return "Equivalente a menos de 1 árbol plantado";
        } else if (arboles.compareTo(new BigDecimal("10")) < 0) {
            return String.format("Equivalente a %.1f árboles plantados", arboles.doubleValue());
        } else {
            return String.format("Equivalente a %.0f árboles plantados", arboles.doubleValue());
        }
    }

    /**
     * Calcula el equivalente de agua ahorrada en términos comprensibles.
     */
    private String calcularEquivalenteAguaAhorrada(Integer litros) {
        if (litros == null || litros == 0) {
            return "Sin agua ahorrada aún";
        }

        // Una ducha promedio usa ~150 litros
        int duchas = litros / 150;
        if (duchas > 0) {
            return String.format("Equivalente a %d ducha%s de 5 minutos", duchas, duchas > 1 ? "s" : "");
        }

        // Botellas de agua de 500ml
        int botellas = litros * 2;
        return String.format("Equivalente a %d botella%s de agua (500ml)", botellas, botellas > 1 ? "s" : "");
    }

    /**
     * Calcula eco-puntos básicos basado en compras y CO₂ ahorrado.
     */
    private Integer calcularEcoPuntosBasico(BigDecimal co2Ahorrado, Integer compras) {
        // Base: 10 puntos por compra
        int puntos = compras * 10;
        
        // Bonus: 1 punto por cada 0.1 kg de CO₂ ahorrado
        if (co2Ahorrado != null && co2Ahorrado.compareTo(BigDecimal.ZERO) > 0) {
            int puntosCO2 = co2Ahorrado.multiply(new BigDecimal("10")).intValue();
            puntos += puntosCO2;
        }
        
        return puntos;
    }

    /**
     * Calcula el nivel de eco-puntos basado en la cantidad de puntos.
     */
    private String calcularNivelEcoPuntos(Integer puntos) {
        if (puntos == null || puntos < 0) {
            return "Sin nivel";
        }
        
        if (puntos >= 501) {
            return "Platino";
        } else if (puntos >= 301) {
            return "Oro";
        } else if (puntos >= 101) {
            return "Plata";
        } else {
            return "Bronce";
        }
    }

    @Override
    public HistorialImpactoResponse obtenerHistorialImpacto(
            Integer usuarioId, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin) {
        
        // Validar que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
        }

        // Obtener pedidos del usuario
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(usuarioId)
                .stream()
                .filter(p -> !"cancelado".equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.toList());

        // Filtrar por fechas si se proporcionan
        if (fechaInicio != null) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getFechaPedido() != null && !p.getFechaPedido().isBefore(fechaInicio))
                    .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getFechaPedido() != null && !p.getFechaPedido().isAfter(fechaFin))
                    .collect(Collectors.toList());
        }

        // Ordenar por fecha descendente (más recientes primero)
        pedidos.sort((p1, p2) -> {
            if (p1.getFechaPedido() == null || p2.getFechaPedido() == null) {
                return 0;
            }
            return p2.getFechaPedido().compareTo(p1.getFechaPedido());
        });

        // Calcular resumen del período
        BigDecimal co2AhorradoTotal = BigDecimal.ZERO;
        Integer aguaAhorradaTotal = 0;
        BigDecimal materialRecicladoTotal = BigDecimal.ZERO;
        BigDecimal huellaCarbonoTotal = BigDecimal.ZERO;

        List<HistorialImpactoResponse.PedidoImpacto> pedidosImpacto = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());
            
            BigDecimal co2AhorradoPedido = BigDecimal.ZERO;
            Integer aguaAhorradaPedido = 0;
            BigDecimal materialRecicladoPedido = BigDecimal.ZERO;
            List<String> nombresProductos = new ArrayList<>();
            Integer cantidadProductos = 0;

            for (PedidoItem item : items) {
                Producto producto = item.getProducto();
                Integer cantidad = item.getCantidad();
                cantidadProductos += cantidad;

                // CO₂ ahorrado
                if (producto.getCo2AhorradoVsConvencional() != null) {
                    BigDecimal co2Item = producto.getCo2AhorradoVsConvencional()
                            .multiply(new BigDecimal(cantidad));
                    co2AhorradoPedido = co2AhorradoPedido.add(co2Item);
                    co2AhorradoTotal = co2AhorradoTotal.add(co2Item);
                }

                // Agua ahorrada
                if (producto.getConsumoAgua() != null) {
                    Integer aguaConvencional = producto.getConsumoAgua() * 2;
                    Integer aguaAhorradaItem = (aguaConvencional - producto.getConsumoAgua()) * cantidad;
                    aguaAhorradaPedido += aguaAhorradaItem;
                    aguaAhorradaTotal += aguaAhorradaItem;
                }

                // Material reciclado
                if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() > 0) {
                    BigDecimal pesoProducto = new BigDecimal("0.5");
                    BigDecimal porcentajeReciclado = new BigDecimal(producto.getPorcentajeReciclable())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal materialItem = pesoProducto
                            .multiply(porcentajeReciclado)
                            .multiply(new BigDecimal(cantidad));
                    materialRecicladoPedido = materialRecicladoPedido.add(materialItem);
                    materialRecicladoTotal = materialRecicladoTotal.add(materialItem);
                }

                nombresProductos.add(producto.getNombre());
            }

            // Huella de carbono del pedido
            BigDecimal huellaPedido = pedido.getHuellaCarbonoTotalKg();
            if (huellaPedido != null) {
                huellaCarbonoTotal = huellaCarbonoTotal.add(huellaPedido);
            }

            pedidosImpacto.add(HistorialImpactoResponse.PedidoImpacto.builder()
                    .pedidoId(pedido.getPedidoId())
                    .fechaPedido(pedido.getFechaPedido())
                    .estado(pedido.getEstado())
                    .total(pedido.getTotal())
                    .huellaCarbono(huellaPedido != null ? huellaPedido : BigDecimal.ZERO)
                    .co2Ahorrado(co2AhorradoPedido.setScale(2, RoundingMode.HALF_UP))
                    .aguaAhorrada(aguaAhorradaPedido)
                    .cantidadProductos(cantidadProductos)
                    .productos(nombresProductos)
                    .build());
        }

        // Determinar fechas del período
        LocalDateTime fechaInicioPeriodo = fechaInicio;
        LocalDateTime fechaFinPeriodo = fechaFin;
        
        if (fechaInicioPeriodo == null && !pedidos.isEmpty()) {
            fechaInicioPeriodo = pedidos.stream()
                    .map(Pedido::getFechaPedido)
                    .filter(f -> f != null)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
        }
        
        if (fechaFinPeriodo == null && !pedidos.isEmpty()) {
            fechaFinPeriodo = pedidos.stream()
                    .map(Pedido::getFechaPedido)
                    .filter(f -> f != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        return HistorialImpactoResponse.builder()
                .usuarioId(usuarioId)
                .fechaInicio(fechaInicioPeriodo)
                .fechaFin(fechaFinPeriodo)
                .resumen(HistorialImpactoResponse.ResumenPeriodo.builder()
                        .totalPedidos(pedidos.size())
                        .co2AhorradoTotal(co2AhorradoTotal.setScale(2, RoundingMode.HALF_UP))
                        .aguaAhorradaTotal(aguaAhorradaTotal)
                        .materialRecicladoTotal(materialRecicladoTotal.setScale(2, RoundingMode.HALF_UP))
                        .huellaCarbonoTotal(huellaCarbonoTotal.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .pedidos(pedidosImpacto)
                .build();
    }

    @Override
    public TendenciasImpactoResponse obtenerTendenciasImpacto(
            Integer usuarioId, 
            String periodo,
            Integer mesesAtras) {
        
        // Validar que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
        }

        // Validar período
        if (periodo == null || periodo.trim().isEmpty()) {
            periodo = "mes"; // Por defecto, mensual
        }
        periodo = periodo.toLowerCase().trim();

        // Validar mesesAtras
        if (mesesAtras == null || mesesAtras <= 0) {
            mesesAtras = 6; // Por defecto, últimos 6 meses
        }

        // Calcular rango de fechas
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusMonths(mesesAtras);

        // Obtener pedidos del usuario en el rango
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(usuarioId)
                .stream()
                .filter(p -> !"cancelado".equalsIgnoreCase(p.getEstado()))
                .filter(p -> p.getFechaPedido() != null 
                        && !p.getFechaPedido().isBefore(fechaInicio)
                        && !p.getFechaPedido().isAfter(fechaFin))
                .collect(Collectors.toList());

        // Agrupar por período
        Map<String, TendenciasImpactoResponse.DatoTendencia> datosAgrupados = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Pedido pedido : pedidos) {
            if (pedido.getFechaPedido() == null) {
                continue;
            }

            String etiqueta = obtenerEtiquetaPeriodo(pedido.getFechaPedido(), periodo);
            String fechaInicioStr = obtenerFechaInicioPeriodo(pedido.getFechaPedido(), periodo).format(formatter);
            String fechaFinStr = obtenerFechaFinPeriodo(pedido.getFechaPedido(), periodo).format(formatter);

            datosAgrupados.putIfAbsent(etiqueta, TendenciasImpactoResponse.DatoTendencia.builder()
                    .etiqueta(etiqueta)
                    .fechaInicio(fechaInicioStr)
                    .fechaFin(fechaFinStr)
                    .co2Ahorrado(BigDecimal.ZERO)
                    .aguaAhorrada(0)
                    .huellaCarbono(BigDecimal.ZERO)
                    .numeroPedidos(0)
                    .materialReciclado(BigDecimal.ZERO)
                    .build());

            TendenciasImpactoResponse.DatoTendencia dato = datosAgrupados.get(etiqueta);

            // Obtener items del pedido
            List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());

            for (PedidoItem item : items) {
                Producto producto = item.getProducto();
                Integer cantidad = item.getCantidad();

                // CO₂ ahorrado
                if (producto.getCo2AhorradoVsConvencional() != null) {
                    BigDecimal co2Item = producto.getCo2AhorradoVsConvencional()
                            .multiply(new BigDecimal(cantidad));
                    dato.setCo2Ahorrado(dato.getCo2Ahorrado().add(co2Item));
                }

                // Agua ahorrada
                if (producto.getConsumoAgua() != null) {
                    Integer aguaConvencional = producto.getConsumoAgua() * 2;
                    Integer aguaAhorradaItem = (aguaConvencional - producto.getConsumoAgua()) * cantidad;
                    dato.setAguaAhorrada(dato.getAguaAhorrada() + aguaAhorradaItem);
                }

                // Material reciclado
                if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() > 0) {
                    BigDecimal pesoProducto = new BigDecimal("0.5");
                    BigDecimal porcentajeReciclado = new BigDecimal(producto.getPorcentajeReciclable())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal materialItem = pesoProducto
                            .multiply(porcentajeReciclado)
                            .multiply(new BigDecimal(cantidad));
                    dato.setMaterialReciclado(dato.getMaterialReciclado().add(materialItem));
                }
            }

            // Huella de carbono
            if (pedido.getHuellaCarbonoTotalKg() != null) {
                dato.setHuellaCarbono(dato.getHuellaCarbono().add(pedido.getHuellaCarbonoTotalKg()));
            }

            dato.setNumeroPedidos(dato.getNumeroPedidos() + 1);
        }

        // Redondear valores
        List<TendenciasImpactoResponse.DatoTendencia> datos = datosAgrupados.values().stream()
                .map(d -> {
                    d.setCo2Ahorrado(d.getCo2Ahorrado().setScale(2, RoundingMode.HALF_UP));
                    d.setHuellaCarbono(d.getHuellaCarbono().setScale(2, RoundingMode.HALF_UP));
                    d.setMaterialReciclado(d.getMaterialReciclado().setScale(2, RoundingMode.HALF_UP));
                    return d;
                })
                .collect(Collectors.toList());

        return TendenciasImpactoResponse.builder()
                .usuarioId(usuarioId)
                .periodo(periodo)
                .datos(datos)
                .build();
    }

    /**
     * Obtiene la etiqueta del período según el tipo de agregación.
     */
    private String obtenerEtiquetaPeriodo(LocalDateTime fecha, String periodo) {
        switch (periodo) {
            case "dia":
                return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "semana":
                int semana = fecha.get(java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).weekOfYear());
                int año = fecha.getYear();
                return String.format("Semana %d, %d", semana, año);
            case "mes":
            default:
                return fecha.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("es")));
        }
    }

    /**
     * Obtiene la fecha de inicio del período.
     */
    private LocalDateTime obtenerFechaInicioPeriodo(LocalDateTime fecha, String periodo) {
        switch (periodo) {
            case "dia":
                return fecha.toLocalDate().atStartOfDay();
            case "semana":
                java.time.DayOfWeek firstDayOfWeek = java.time.DayOfWeek.MONDAY;
                int daysToSubtract = fecha.getDayOfWeek().getValue() - firstDayOfWeek.getValue();
                if (daysToSubtract < 0) {
                    daysToSubtract += 7;
                }
                return fecha.minusDays(daysToSubtract).toLocalDate().atStartOfDay();
            case "mes":
            default:
                return fecha.withDayOfMonth(1).toLocalDate().atStartOfDay();
        }
    }

    /**
     * Obtiene la fecha de fin del período.
     */
    private LocalDateTime obtenerFechaFinPeriodo(LocalDateTime fecha, String periodo) {
        switch (periodo) {
            case "dia":
                return fecha.toLocalDate().atTime(23, 59, 59);
            case "semana":
                LocalDateTime inicioSemana = obtenerFechaInicioPeriodo(fecha, periodo);
                return inicioSemana.plusDays(6).toLocalDate().atTime(23, 59, 59);
            case "mes":
            default:
                return fecha.withDayOfMonth(fecha.toLocalDate().lengthOfMonth())
                        .toLocalDate()
                        .atTime(23, 59, 59);
        }
    }

    @Override
    public com.ecoshop.dto.Producto.TrazabilidadProductoResponse obtenerTrazabilidadProducto(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        // Asegurar que las métricas estén calculadas
        if (producto.getEmisionesFabricacion() == null || producto.getEmisionesEmpaque() == null ||
            producto.getEmisionesTransporte() == null || producto.getEmisionesEntrega() == null) {
            producto = calcularYAlmacenarMetricasDetalladas(producto);
            productoRepository.save(producto);
        }

        List<com.ecoshop.dto.Producto.TrazabilidadProductoResponse.EtapaTrazabilidad> etapas = new ArrayList<>();

        // Etapa 1: Origen
        String paisOrigen = producto.getOrigenPais() != null ? producto.getOrigenPais() : 
                           (producto.getOrigen() != null ? producto.getOrigen() : "No especificado");
        String descripcionOrigen = generarDescripcionOrigen(producto);
        BigDecimal co2Origen = producto.getEmisionesFabricacion() != null ? 
                              producto.getEmisionesFabricacion() : BigDecimal.ZERO;

        etapas.add(com.ecoshop.dto.Producto.TrazabilidadProductoResponse.EtapaTrazabilidad.builder()
                .etapa("Origen")
                .pais(paisOrigen)
                .descripcion(descripcionOrigen)
                .co2(co2Origen)
                .build());

        // Etapa 2: Empaque
        String descripcionEmpaque = generarDescripcionEmpaque(producto);
        BigDecimal co2Empaque = producto.getEmisionesEmpaque() != null ? 
                               producto.getEmisionesEmpaque() : BigDecimal.ZERO;

        etapas.add(com.ecoshop.dto.Producto.TrazabilidadProductoResponse.EtapaTrazabilidad.builder()
                .etapa("Empaque")
                .ubicacion("Centro de distribución")
                .descripcion(descripcionEmpaque)
                .co2(co2Empaque)
                .build());

        // Etapa 3: Transporte
        String descripcionTransporte = generarDescripcionTransporte(producto);
        BigDecimal co2Transporte = producto.getEmisionesTransporte() != null ? 
                                   producto.getEmisionesTransporte() : BigDecimal.ZERO;
        Integer distancia = producto.getDistanciaTransporte() != null ? 
                           producto.getDistanciaTransporte() : 0;
        Boolean logisticaOptimizada = producto.getLogisticaOptimizada() != null ? 
                                      producto.getLogisticaOptimizada() : false;

        etapas.add(com.ecoshop.dto.Producto.TrazabilidadProductoResponse.EtapaTrazabilidad.builder()
                .etapa("Transporte")
                .descripcion(descripcionTransporte)
                .co2(co2Transporte)
                .distancia(distancia)
                .optimizado(logisticaOptimizada)
                .build());

        // Etapa 4: Entrega
        String descripcionEntrega = generarDescripcionEntrega(producto);
        BigDecimal co2Entrega = producto.getEmisionesEntrega() != null ? 
                               producto.getEmisionesEntrega() : BigDecimal.ZERO;
        Boolean ultimaMillaCarbonoNeutral = producto.getUltimaMillaCarbonoNeutral() != null ? 
                                            producto.getUltimaMillaCarbonoNeutral() : false;

        etapas.add(com.ecoshop.dto.Producto.TrazabilidadProductoResponse.EtapaTrazabilidad.builder()
                .etapa("Entrega")
                .ubicacion("Tu hogar")
                .descripcion(descripcionEntrega)
                .co2(co2Entrega)
                .optimizado(ultimaMillaCarbonoNeutral)
                .build());

        // Calcular huella total verificada (suma de todas las etapas)
        BigDecimal huellaTotalVerificada = co2Origen
                .add(co2Empaque)
                .add(co2Transporte)
                .add(co2Entrega);

        return com.ecoshop.dto.Producto.TrazabilidadProductoResponse.builder()
                .productoId(producto.getProductoId())
                .nombreProducto(producto.getNombre())
                .huellaTotalVerificada(huellaTotalVerificada)
                .etapas(etapas)
                .build();
    }

    /**
     * Genera una descripción detallada de la etapa de origen.
     */
    private String generarDescripcionOrigen(Producto producto) {
        StringBuilder descripcion = new StringBuilder();
        
        if (producto.getMateriales() != null && !producto.getMateriales().isEmpty()) {
            descripcion.append("Fabricado con ").append(producto.getMateriales());
        } else {
            descripcion.append("Fabricado de forma sostenible");
        }

        if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() > 0) {
            descripcion.append(", ").append(producto.getPorcentajeReciclable()).append("% reciclable");
        }

        return descripcion.toString();
    }

    /**
     * Genera una descripción detallada de la etapa de empaque.
     */
    private String generarDescripcionEmpaque(Producto producto) {
        StringBuilder descripcion = new StringBuilder();
        
        if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 80) {
            descripcion.append("100% materiales reciclables");
        } else if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 50) {
            descripcion.append("Mayormente materiales reciclables");
        } else {
            descripcion.append("Empaque sostenible");
        }

        return descripcion.toString();
    }

    /**
     * Genera una descripción detallada de la etapa de transporte.
     */
    private String generarDescripcionTransporte(Producto producto) {
        StringBuilder descripcion = new StringBuilder();
        
        if (producto.getLogisticaOptimizada() != null && producto.getLogisticaOptimizada()) {
            descripcion.append("Logística optimizada");
        } else {
            descripcion.append("Transporte estándar");
        }

        if (producto.getDistanciaTransporte() != null && producto.getDistanciaTransporte() > 0) {
            descripcion.append(" (").append(producto.getDistanciaTransporte()).append(" km)");
        }

        return descripcion.toString();
    }

    /**
     * Genera una descripción detallada de la etapa de entrega.
     */
    private String generarDescripcionEntrega(Producto producto) {
        if (producto.getUltimaMillaCarbonoNeutral() != null && producto.getUltimaMillaCarbonoNeutral()) {
            return "Última milla carbono neutral";
        } else {
            return "Entrega estándar";
        }
    }
}