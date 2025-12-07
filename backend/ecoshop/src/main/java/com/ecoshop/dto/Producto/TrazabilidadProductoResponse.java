package com.ecoshop.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la respuesta de trazabilidad completa de un producto.
 * 
 * Muestra el viaje completo del producto desde su origen hasta la entrega,
 * incluyendo las emisiones de CO₂ por cada etapa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrazabilidadProductoResponse {

    private Integer productoId;
    private String nombreProducto;
    private BigDecimal huellaTotalVerificada;
    private List<EtapaTrazabilidad> etapas;

    /**
     * Representa una etapa del viaje del producto.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtapaTrazabilidad {
        private String etapa; // "Origen", "Empaque", "Transporte", "Entrega"
        private String pais; // País de origen (solo para etapa Origen)
        private String ubicacion; // Ubicación de la etapa (solo para Empaque y Entrega)
        private String descripcion; // Descripción detallada de la etapa
        private BigDecimal co2; // Emisiones de CO₂ en kg para esta etapa
        private Integer distancia; // Distancia en km (solo para Transporte)
        private Boolean optimizado; // Indica si está optimizado (solo para Transporte y Entrega)
    }
}

