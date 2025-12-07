package com.ecoshop.service.carrito.impl;

import com.ecoshop.domain.Carrito;
import com.ecoshop.domain.CarritoItem;
import com.ecoshop.domain.Producto;
import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Carrito.CarritoItemRequestDTO;
import com.ecoshop.dto.Carrito.CarritoItemResponseDTO;
import com.ecoshop.dto.Carrito.CarritoResponseDTO;
import com.ecoshop.dto.Carrito.ImpactoAmbientalCarritoResponse;
import com.ecoshop.exception.BadRequestException;
import com.ecoshop.exception.ForbiddenException;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.mapper.CarritoMapper;
import com.ecoshop.repository.carrito.CarritoItemRepository;
import com.ecoshop.repository.carrito.CarritoRepository;
import com.ecoshop.repository.producto.ProductoRepository;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.carrito.CarritoService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementación del servicio de carritos de compras.
 * 
 * Esta clase contiene la lógica de negocio para gestionar carritos.
 * El carrito persiste en la base de datos para usuarios autenticados.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CarritoMapper carritoMapper;
    private final ImpactoAmbientalService impactoAmbientalService;

    @Override
    @Transactional
    public CarritoResponseDTO obtenerCarrito(Integer usuarioId) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Obtener o crear el carrito
        Carrito carrito = carritoRepository.findByUsuario_UsuarioId(usuarioId)
                .orElseGet(() -> {
                    // Si no existe, crear uno nuevo
                    Carrito nuevoCarrito = Carrito.builder()
                            .usuario(usuario)
                            .total(BigDecimal.ZERO)
                            .build();
                    return carritoRepository.save(nuevoCarrito);
                });

        // Obtener items del carrito
        List<CarritoItem> items = carritoItemRepository.findByCarrito_CarritoId(carrito.getCarritoId());
        
        // Recalcular total
        recalcularTotal(carrito);

        // Convertir a DTO
        List<CarritoItemResponseDTO> itemsDTO = carritoMapper.toItemResponseList(items);
        return carritoMapper.toResponse(carrito, itemsDTO);
    }

    @Override
    @Transactional
    public CarritoResponseDTO agregarItem(Integer usuarioId, CarritoItemRequestDTO request) {
        // Obtener o crear el carrito
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        // Validar que el producto existe
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getProductoId()));

        // Verificar si el producto ya está en el carrito
        CarritoItem itemExistente = carritoItemRepository
                .findByCarrito_CarritoIdAndProducto_ProductoId(carrito.getCarritoId(), request.getProductoId())
                .orElse(null);

        if (itemExistente != null) {
            // Si ya existe, actualizar la cantidad
            itemExistente.setCantidad(itemExistente.getCantidad() + request.getCantidad());
            carritoItemRepository.save(itemExistente);
        } else {
            // Si no existe, crear un nuevo item
            CarritoItem nuevoItem = CarritoItem.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .cantidad(request.getCantidad())
                    .precioUnitario(producto.getPrecio()) // Congelar precio
                    .build();
            carritoItemRepository.save(nuevoItem);
        }

        // Recalcular total
        recalcularTotal(carrito);

        // Retornar carrito actualizado
        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public CarritoResponseDTO actualizarCantidadItem(Integer usuarioId, Integer itemId, Integer cantidad) {
        // Validar cantidad
        if (cantidad == null || cantidad < 1) {
            throw new BadRequestException("La cantidad debe ser al menos 1");
        }

        // Obtener el carrito del usuario
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        // Obtener el item
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado con ID: " + itemId));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getCarritoId().equals(carrito.getCarritoId())) {
            throw new ForbiddenException(
                String.format("No tienes permisos para actualizar este item. " +
                            "El item pertenece al carrito del usuario con ID %d. Tu usuario tiene ID %d.",
                            item.getCarrito().getUsuario().getUsuarioId(), usuarioId)
            );
        }

        // Actualizar cantidad
        item.setCantidad(cantidad);
        carritoItemRepository.save(item);

        // Recalcular total
        recalcularTotal(carrito);

        // Retornar carrito actualizado
        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public CarritoResponseDTO eliminarItem(Integer usuarioId, Integer itemId) {
        // Obtener el carrito del usuario
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        // Obtener el item
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado con ID: " + itemId));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getCarritoId().equals(carrito.getCarritoId())) {
            throw new ForbiddenException(
                String.format("No tienes permisos para eliminar este item. " +
                            "El item pertenece al carrito del usuario con ID %d. Tu usuario tiene ID %d.",
                            item.getCarrito().getUsuario().getUsuarioId(), usuarioId)
            );
        }

        // Eliminar el item
        carritoItemRepository.delete(item);

        // Recalcular total
        recalcularTotal(carrito);

        // Retornar carrito actualizado
        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public CarritoResponseDTO vaciarCarrito(Integer usuarioId) {
        // Obtener el carrito del usuario
        // Nota: obtenerOCrearCarrito solo devuelve el carrito del usuario especificado,
        // por lo que está protegido contra acceso no autorizado
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        
        // Verificar explícitamente que el carrito pertenece al usuario (validación adicional de seguridad)
        if (!carrito.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException(
                String.format("No tienes permisos para vaciar este carrito. " +
                            "El carrito pertenece al usuario con ID %d. Tu usuario tiene ID %d.",
                            carrito.getUsuario().getUsuarioId(), usuarioId)
            );
        }

        // Eliminar todos los items
        carritoItemRepository.deleteByCarrito_CarritoId(carrito.getCarritoId());

        // Recalcular total (debe ser 0)
        recalcularTotal(carrito);

        // Retornar carrito vacío
        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public void eliminarCarrito(Integer usuarioId) {
        // Obtener el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuario_UsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado para el usuario con ID: " + usuarioId));
        
        // Verificar explícitamente que el carrito pertenece al usuario (validación de seguridad)
        if (!carrito.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException(
                String.format("No tienes permisos para eliminar este carrito. " +
                            "El carrito pertenece al usuario con ID %d. Tu usuario tiene ID %d.",
                            carrito.getUsuario().getUsuarioId(), usuarioId)
            );
        }

        // Eliminar todos los items del carrito primero (cascade delete debería hacerlo, pero lo hacemos explícitamente)
        carritoItemRepository.deleteByCarrito_CarritoId(carrito.getCarritoId());
        
        // Eliminar el carrito completo
        carritoRepository.delete(carrito);
    }

    @Override
    @Transactional(readOnly = true)
    public ImpactoAmbientalCarritoResponse calcularImpactoAmbiental(Integer usuarioId) {
        // Obtener el carrito
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        List<CarritoItem> items = carritoItemRepository.findByCarrito_CarritoId(carrito.getCarritoId());

        if (items.isEmpty()) {
            return ImpactoAmbientalCarritoResponse.builder()
                    .huellaCarbonoTotal(BigDecimal.ZERO)
                    .co2Ahorrado(BigDecimal.ZERO)
                    .equivalente("El carrito está vacío")
                    .build();
        }

        // Calcular huella de carbono total
        BigDecimal huellaCarbonoTotal = BigDecimal.ZERO;
        BigDecimal co2Ahorrado = BigDecimal.ZERO;

        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();

            // Calcular huella de carbono del producto
            BigDecimal huellaProducto = impactoAmbientalService.calcularHuellaCarbonoProducto(producto);
            huellaCarbonoTotal = huellaCarbonoTotal.add(huellaProducto.multiply(new BigDecimal(cantidad)));

            // Calcular CO₂ ahorrado
            if (producto.getCo2AhorradoVsConvencional() != null) {
                co2Ahorrado = co2Ahorrado.add(
                        producto.getCo2AhorradoVsConvencional().multiply(new BigDecimal(cantidad))
                );
            }
        }

        // Calcular equivalente
        String equivalente = calcularEquivalente(huellaCarbonoTotal);

        return ImpactoAmbientalCarritoResponse.builder()
                .huellaCarbonoTotal(huellaCarbonoTotal.setScale(2, RoundingMode.HALF_UP))
                .co2Ahorrado(co2Ahorrado.setScale(2, RoundingMode.HALF_UP))
                .equivalente(equivalente)
                .build();
    }

    /**
     * Obtiene o crea el carrito del usuario.
     */
    private Carrito obtenerOCrearCarrito(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        return carritoRepository.findByUsuario_UsuarioId(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = Carrito.builder()
                            .usuario(usuario)
                            .total(BigDecimal.ZERO)
                            .build();
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    /**
     * Recalcula el total del carrito sumando todos los items.
     */
    private void recalcularTotal(Carrito carrito) {
        List<CarritoItem> items = carritoItemRepository.findByCarrito_CarritoId(carrito.getCarritoId());
        
        BigDecimal total = items.stream()
                .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        carrito.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        carritoRepository.save(carrito);
    }

    /**
     * Calcula el equivalente de la huella de carbono en términos comprensibles.
     */
    private String calcularEquivalente(BigDecimal kgCO2) {
        if (kgCO2 == null || kgCO2.compareTo(BigDecimal.ZERO) == 0) {
            return "Sin emisiones";
        }

        // Un km en auto promedio emite aproximadamente 0.12 kg CO₂
        BigDecimal kmAuto = kgCO2.divide(new BigDecimal("0.12"), 1, RoundingMode.HALF_UP);
        
        if (kmAuto.compareTo(new BigDecimal("1")) < 0) {
            return "Equivalente a menos de 1 km en auto";
        } else if (kmAuto.compareTo(new BigDecimal("10")) < 0) {
            return String.format("Equivalente a %.1f km en auto", kmAuto.doubleValue());
        } else {
            return String.format("Equivalente a %.0f km en auto", kmAuto.doubleValue());
        }
    }
}

