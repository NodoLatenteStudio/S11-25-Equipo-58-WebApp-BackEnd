package com.ecoshop.service.usuario.impl;

import com.ecoshop.domain.Pedido;
import com.ecoshop.domain.PedidoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.EcoPuntos.CalculoEcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.EcoPuntosResponse;
import com.ecoshop.dto.EcoPuntos.HistorialEcoPuntosResponse;
import com.ecoshop.enums.EcoBadge;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.pedidoitem.PedidoItemRepository;
import com.ecoshop.repository.pedido.PedidoRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.usuario.EcoPuntosService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de eco-puntos.
 * 
 * Calcula y gestiona los eco-puntos de los usuarios basándose en:
 * - Monto de compra (puntos base)
 * - Productos con bajo impacto (bonus)
 * - Material reciclado (bonus)
 * - Certificaciones (bonus)
 */
@Service
@Transactional
public class EcoPuntosServiceImpl implements EcoPuntosService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final ImpactoAmbientalService impactoAmbientalService;

    public EcoPuntosServiceImpl(
            UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository,
            PedidoItemRepository pedidoItemRepository,
            @Lazy ImpactoAmbientalService impactoAmbientalService) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.impactoAmbientalService = impactoAmbientalService;
    }

    // Factores de cálculo de puntos
    private static final double PUNTOS_POR_PESO = 0.1; // 0.1 puntos por cada peso gastado
    private static final int BONUS_BAJO_IMPACTO = 20; // 20 puntos extra por producto con bajo impacto
    private static final int BONUS_RECICLABLE = 10; // 10 puntos extra por producto 100% reciclable
    private static final int BONUS_CERTIFICACION = 5; // 5 puntos extra por certificación

    @Override
    @Transactional(readOnly = true)
    public CalculoEcoPuntosResponse calcularPuntosPedido(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedidoId);
        
        if (items.isEmpty()) {
            return CalculoEcoPuntosResponse.builder()
                    .pedidoId(pedidoId)
                    .puntosTotales(0)
                    .puntosBase(0)
                    .puntosBonus(0)
                    .descripcion("No hay productos en el pedido")
                    .items(new ArrayList<>())
                    .build();
        }

        int puntosBase = 0;
        int puntosBonus = 0;
        List<CalculoEcoPuntosResponse.ItemPedido> itemsPuntos = new ArrayList<>();

        // Calcular puntos base por monto
        if (pedido.getTotal() != null) {
            puntosBase = (int) (pedido.getTotal().doubleValue() * PUNTOS_POR_PESO);
        }

        // Calcular puntos por cada producto
        for (PedidoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();
            int puntosItem = 0;
            List<String> motivos = new ArrayList<>();

            // Bonus por bajo impacto
            EcoBadge ecoBadge = impactoAmbientalService.calcularEcoBadge(producto);
            if (ecoBadge == EcoBadge.BAJO_IMPACTO) {
                puntosItem += BONUS_BAJO_IMPACTO * cantidad;
                motivos.add("Producto con bajo impacto");
            }

            // Bonus por material reciclable
            if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 100) {
                puntosItem += BONUS_RECICLABLE * cantidad;
                motivos.add("Producto 100% reciclable");
            } else if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 50) {
                puntosItem += (BONUS_RECICLABLE / 2) * cantidad;
                motivos.add("Producto parcialmente reciclable");
            }

            // Bonus por certificaciones
            if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
                int puntosCertificaciones = producto.getCertificaciones().size() * BONUS_CERTIFICACION * cantidad;
                puntosItem += puntosCertificaciones;
                motivos.add(producto.getCertificaciones().size() + " certificación(es) ambiental(es)");
            }

            puntosBonus += puntosItem;

            itemsPuntos.add(CalculoEcoPuntosResponse.ItemPedido.builder()
                    .productoId(producto.getProductoId())
                    .nombreProducto(producto.getNombre())
                    .cantidad(cantidad)
                    .puntos(puntosItem)
                    .motivo(String.join(", ", motivos))
                    .build());
        }

        int puntosTotales = puntosBase + puntosBonus;

        String descripcion = String.format(
                "Ganarás %d puntos: %d puntos base (por el monto de compra) + %d puntos bonus (por productos sostenibles)",
                puntosTotales, puntosBase, puntosBonus);

        return CalculoEcoPuntosResponse.builder()
                .pedidoId(pedidoId)
                .puntosTotales(puntosTotales)
                .puntosBase(puntosBase)
                .puntosBonus(puntosBonus)
                .descripcion(descripcion)
                .items(itemsPuntos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EcoPuntosResponse obtenerEcoPuntos(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Integer ecoPuntos = usuario.getEcoPuntos() != null ? usuario.getEcoPuntos() : 0;
        String nivelActual = calcularNivelEcoPuntos(ecoPuntos);
        Integer metaSiguienteNivel = calcularMetaSiguienteNivel(ecoPuntos);
        Integer puntosParaSiguiente = metaSiguienteNivel - ecoPuntos;
        
        double porcentajeProgreso = 0.0;
        if (metaSiguienteNivel > 0) {
            porcentajeProgreso = (double) ecoPuntos / metaSiguienteNivel * 100;
            porcentajeProgreso = Math.min(100.0, Math.max(0.0, porcentajeProgreso));
        }

        return EcoPuntosResponse.builder()
                .usuarioId(usuarioId)
                .ecoPuntos(ecoPuntos)
                .nivelEcoPuntos(nivelActual)
                .metaEcoPuntos(metaSiguienteNivel)
                .puntosParaSiguienteNivel(puntosParaSiguiente)
                .porcentajeProgreso(redondear(porcentajeProgreso, 2))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialEcoPuntosResponse obtenerHistorialEcoPuntos(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        // Obtener pedidos entregados del usuario
        List<Pedido> pedidos = pedidoRepository.findByUsuario_UsuarioId(usuarioId)
                .stream()
                .filter(p -> "entregado".equalsIgnoreCase(p.getEstado()))
                .sorted(Comparator.comparing(Pedido::getFechaPedido).reversed())
                .collect(Collectors.toList());

        List<HistorialEcoPuntosResponse.RegistroPuntos> registros = new ArrayList<>();
        Integer totalPuntos = usuario.getEcoPuntos() != null ? usuario.getEcoPuntos() : 0;

        for (Pedido pedido : pedidos) {
            List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());
            
            int puntosPedido = calcularPuntosItems(items, pedido.getTotal());
            
            if (puntosPedido > 0) {
                String motivo = generarMotivoPuntos(items, puntosPedido);
                String descripcion = generarDescripcionPuntos(items, pedido.getTotal());
                
                registros.add(HistorialEcoPuntosResponse.RegistroPuntos.builder()
                        .pedidoId(pedido.getPedidoId())
                        .fecha(pedido.getFechaPedido())
                        .puntosGanados(puntosPedido)
                        .motivo(motivo)
                        .descripcion(descripcion)
                        .build());
            }
        }

        return HistorialEcoPuntosResponse.builder()
                .usuarioId(usuarioId)
                .totalPuntos(totalPuntos)
                .registros(registros)
                .build();
    }

    @Override
    public Integer asignarPuntosPorPedido(Pedido pedido) {
        if (pedido == null || pedido.getUsuario() == null) {
            return 0;
        }

        // Solo asignar puntos si el pedido está entregado
        if (!"entregado".equalsIgnoreCase(pedido.getEstado())) {
            return 0;
        }

        List<PedidoItem> items = pedidoItemRepository.findByPedido_PedidoId(pedido.getPedidoId());
        Integer puntosGanados = calcularPuntosItems(items, pedido.getTotal());

        if (puntosGanados > 0) {
            Usuario usuario = pedido.getUsuario();
            Integer puntosActuales = usuario.getEcoPuntos() != null ? usuario.getEcoPuntos() : 0;
            Integer nuevosPuntos = puntosActuales + puntosGanados;
            
            usuario.setEcoPuntos(nuevosPuntos);
            usuario.setNivelEcoPuntos(calcularNivelEcoPuntos(nuevosPuntos));
            usuario.setMetaEcoPuntos(calcularMetaSiguienteNivel(nuevosPuntos));
            
            usuarioRepository.save(usuario);
        }

        return puntosGanados;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularPuntosItems(List<CalculoEcoPuntosResponse.ItemPedido> items) {
        // Este método es para calcular puntos de items que vienen del DTO
        // Se usa cuando se calcula antes de crear el pedido
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int puntos = 0;
        for (CalculoEcoPuntosResponse.ItemPedido item : items) {
            puntos += item.getPuntos();
        }
        return puntos;
    }

    /**
     * Calcula los puntos de una lista de PedidoItem.
     */
    private Integer calcularPuntosItems(List<PedidoItem> items, BigDecimal totalPedido) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        int puntosBase = 0;
        if (totalPedido != null) {
            puntosBase = (int) (totalPedido.doubleValue() * PUNTOS_POR_PESO);
        }

        int puntosBonus = 0;
        for (PedidoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();

            // Bonus por bajo impacto
            EcoBadge ecoBadge = impactoAmbientalService.calcularEcoBadge(producto);
            if (ecoBadge == EcoBadge.BAJO_IMPACTO) {
                puntosBonus += BONUS_BAJO_IMPACTO * cantidad;
            }

            // Bonus por material reciclable
            if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 100) {
                puntosBonus += BONUS_RECICLABLE * cantidad;
            } else if (producto.getPorcentajeReciclable() != null && producto.getPorcentajeReciclable() >= 50) {
                puntosBonus += (BONUS_RECICLABLE / 2) * cantidad;
            }

            // Bonus por certificaciones
            if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
                puntosBonus += producto.getCertificaciones().size() * BONUS_CERTIFICACION * cantidad;
            }
        }

        return puntosBase + puntosBonus;
    }

    /**
     * Calcula el nivel de eco-puntos basado en la cantidad de puntos.
     */
    private String calcularNivelEcoPuntos(Integer puntos) {
        if (puntos == null || puntos < 0) {
            return "Bronce";
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

    /**
     * Calcula la meta de puntos para el siguiente nivel.
     */
    private Integer calcularMetaSiguienteNivel(Integer puntos) {
        if (puntos == null || puntos < 0) {
            return 101; // Meta para Plata
        }
        
        if (puntos < 101) {
            return 101; // Meta para Plata
        } else if (puntos < 301) {
            return 301; // Meta para Oro
        } else if (puntos < 501) {
            return 501; // Meta para Platino
        } else {
            // Ya está en Platino, la meta es mantener el nivel
            return 501;
        }
    }

    /**
     * Genera el motivo principal de los puntos ganados.
     */
    private String generarMotivoPuntos(List<PedidoItem> items, Integer puntos) {
        if (items == null || items.isEmpty()) {
            return "Compra sostenible";
        }

        long productosBajoImpacto = items.stream()
                .map(PedidoItem::getProducto)
                .filter(p -> {
                    EcoBadge badge = impactoAmbientalService.calcularEcoBadge(p);
                    return badge == EcoBadge.BAJO_IMPACTO;
                })
                .count();

        if (productosBajoImpacto > 0) {
            return "Compra con productos de bajo impacto";
        }

        return "Compra sostenible";
    }

    /**
     * Genera una descripción detallada de cómo se ganaron los puntos.
     */
    private String generarDescripcionPuntos(List<PedidoItem> items, BigDecimal total) {
        List<String> partes = new ArrayList<>();

        if (total != null) {
            int puntosBase = (int) (total.doubleValue() * PUNTOS_POR_PESO);
            partes.add(String.format("%d puntos por monto de compra", puntosBase));
        }

        long productosBajoImpacto = items.stream()
                .map(PedidoItem::getProducto)
                .filter(p -> {
                    EcoBadge badge = impactoAmbientalService.calcularEcoBadge(p);
                    return badge == EcoBadge.BAJO_IMPACTO;
                })
                .count();

        if (productosBajoImpacto > 0) {
            partes.add(String.format("%d producto(s) con bajo impacto", productosBajoImpacto));
        }

        long productosReciclables = items.stream()
                .map(PedidoItem::getProducto)
                .filter(p -> p.getPorcentajeReciclable() != null && p.getPorcentajeReciclable() >= 50)
                .count();

        if (productosReciclables > 0) {
            partes.add(String.format("%d producto(s) reciclable(s)", productosReciclables));
        }

        long totalCertificaciones = items.stream()
                .map(PedidoItem::getProducto)
                .filter(p -> p.getCertificaciones() != null && !p.getCertificaciones().isEmpty())
                .mapToLong(p -> p.getCertificaciones().size())
                .sum();

        if (totalCertificaciones > 0) {
            partes.add(String.format("%d certificación(es) ambiental(es)", totalCertificaciones));
        }

        return String.join(" + ", partes);
    }

    /**
     * Redondea un número a un número específico de decimales.
     */
    private double redondear(double valor, int decimales) {
        BigDecimal bd = new BigDecimal(Double.toString(valor));
        bd = bd.setScale(decimales, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

