package com.ecoshop.mapper;

import com.ecoshop.domain.Certificacion;
import com.ecoshop.domain.Producto;
import com.ecoshop.dto.Producto.ProductoRequestDTO;
import com.ecoshop.dto.Producto.ProductoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Producto y DTOs.
 * 
 * Esta clase se encarga de convertir entre:
 * - Producto (entidad JPA) ↔ ProductoRequestDTO (datos de entrada)
 * - Producto (entidad JPA) ↔ ProductoResponseDTO (datos de salida)
 * 
 * ¿Por qué usar un Mapper?
 * - Separación de responsabilidades: el mapper se encarga solo de la conversión
 * - Reutilización: el mismo mapper se usa en diferentes partes del código
 * - Mantenibilidad: cambios en la estructura se hacen en un solo lugar
 * 
 * Conversiones realizadas:
 * 1. toEntity: Convierte ProductoRequestDTO a Producto (sin certificaciones ni marca)
 * 2. toResponse: Convierte Producto a ProductoResponseDTO (incluye certificaciones como nombres)
 * 3. toDto: Convierte Producto a ProductoRequestDTO (para casos especiales)
 * 
 * Notas importantes:
 * - Las certificaciones se manejan de forma especial:
 *   - En ProductoRequestDTO: se usan códigos (ej: ["FAIR_TRADE", "CARBON_NEUTRAL"])
 *   - En ProductoResponseDTO: se usan nombres (ej: ["Fair Trade", "Carbon Neutral"])
 * - La marca no se mapea desde el DTO, se asigna en el servicio después de validar su existencia
 * - Los campos calculados (productoId, fechaCreacion) se manejan automáticamente por JPA
 */
@Component // Indica a Spring que esta clase es un componente (bean de Spring)
public class ProductoMapper {

    /**
     * Convierte un ProductoRequestDTO a una entidad Producto.
     * 
     * Este método crea una nueva entidad Producto a partir de los datos del DTO.
     * NO incluye las certificaciones ni la marca, ya que estos se asignan
     * en el servicio después de validar su existencia.
     * 
     * Proceso:
     * 1. Crea una nueva instancia de Producto
     * 2. Copia los campos básicos del DTO a la entidad
     * 3. Establece valores por defecto para campos opcionales
     * 4. Retorna la entidad (sin ID, sin certificaciones, sin marca)
     * 
     * @param dto ProductoRequestDTO con los datos del producto
     * @return Producto entidad sin ID, certificaciones ni marca asignadas
     * 
     * Nota: La marca y las certificaciones deben asignarse en el servicio
     * después de validar su existencia en la base de datos.
     */
    public Producto toEntity(ProductoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .sku(dto.getSku())
                .materiales(dto.getMateriales())
                .origen(dto.getOrigen())
                .huellaCarbonoTotal(dto.getHuellaCarbonoTotal())
                .porcentajeReciclable(dto.getPorcentajeReciclable() != null ? dto.getPorcentajeReciclable() : 0)
                .ecoBadge(dto.getEcoBadge())
                .imagenUrl(dto.getImagenUrl())
                .activo(true) // Por defecto, los productos nuevos están activos
                .build();
    }

    /**
     * Convierte una entidad Producto a un ProductoResponseDTO.
     * 
     * Este método crea un DTO de respuesta a partir de la entidad Producto.
     * Incluye todos los campos, incluyendo la marca y las certificaciones.
     * 
     * Proceso:
     * 1. Crea un nuevo ProductoResponseDTO
     * 2. Copia los campos básicos de la entidad al DTO
     * 3. Extrae el ID y nombre de la marca
     * 4. Convierte las certificaciones de entidades a nombres (para el frontend)
     * 5. Retorna el DTO completo
     * 
     * Conversión de certificaciones:
     * - Las certificaciones se convierten de Set<Certificacion> a List<String>
     * - Se extraen los nombres (no los códigos) para facilitar la visualización en el frontend
     * - Se filtran certificaciones null o con nombres vacíos
     * 
     * @param producto Entidad Producto con todos sus datos cargados
     * @return ProductoResponseDTO con todos los datos del producto
     * 
     * Nota: La entidad Producto debe tener la marca y certificaciones cargadas
     * (usando @EntityGraph en el repositorio) para que este método funcione correctamente.
     */
    public ProductoResponseDTO toResponse(Producto producto) {
        if (producto == null) {
            return null;
        }

        // Extraer nombres de certificaciones (para el frontend)
        List<String> nombresCertificaciones = null;
        if (producto.getCertificaciones() != null && !producto.getCertificaciones().isEmpty()) {
            nombresCertificaciones = producto.getCertificaciones().stream()
                    .map(Certificacion::getNombreSello)
                    .filter(nombre -> nombre != null && !nombre.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        return ProductoResponseDTO.builder()
                .productoId(producto.getProductoId())
                .marcaId(producto.getMarca() != null ? producto.getMarca().getMarcaId() : null)
                .nombreMarca(producto.getMarca() != null ? producto.getMarca().getNombreOficial() : null)
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .sku(producto.getSku())
                .materiales(producto.getMateriales())
                .origen(producto.getOrigen())
                .huellaCarbonoTotal(producto.getHuellaCarbonoTotal())
                .porcentajeReciclable(producto.getPorcentajeReciclable())
                .ecoBadge(producto.getEcoBadge())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .fechaCreacion(producto.getFechaCreacion())
                .certificaciones(nombresCertificaciones)
                .build();
    }

    /**
     * Actualiza una entidad Producto existente con los datos de un ProductoRequestDTO.
     * 
     * Este método actualiza los campos de una entidad Producto existente
     * con los valores de un DTO, preservando campos importantes como el ID
     * y la fecha de creación.
     * 
     * Proceso:
     * 1. Actualiza los campos básicos del producto desde el DTO
     * 2. NO actualiza el ID (se mantiene el original)
     * 3. NO actualiza la fecha de creación (se preserva la original)
     * 4. NO actualiza la marca ni las certificaciones (se manejan en el servicio)
     * 
     * @param producto Entidad Producto existente a actualizar
     * @param dto ProductoRequestDTO con los nuevos datos
     * 
     * Nota: Este método solo actualiza los campos básicos. La marca y las
     * certificaciones deben actualizarse en el servicio después de validar su existencia.
     */
    public void updateEntityFromDto(Producto producto, ProductoRequestDTO dto) {
        if (producto == null || dto == null) {
            return;
        }

        // Actualizar campos básicos solo si no son null (partial update)
        // Esto permite actualizar solo los campos que se envían en el DTO
        // preservando los valores existentes de los campos no enviados
        
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            producto.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            producto.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getPrecio() != null) {
            producto.setPrecio(dto.getPrecio());
        }
        
        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }
        
        if (dto.getMateriales() != null) {
            producto.setMateriales(dto.getMateriales());
        }
        
        if (dto.getOrigen() != null) {
            producto.setOrigen(dto.getOrigen());
        }
        
        if (dto.getHuellaCarbonoTotal() != null) {
            producto.setHuellaCarbonoTotal(dto.getHuellaCarbonoTotal());
        }
        
        if (dto.getPorcentajeReciclable() != null) {
            producto.setPorcentajeReciclable(dto.getPorcentajeReciclable());
        }
        
        if (dto.getEcoBadge() != null) {
            producto.setEcoBadge(dto.getEcoBadge());
        }
        
        if (dto.getImagenUrl() != null) {
            producto.setImagenUrl(dto.getImagenUrl());
        }
    }
}

