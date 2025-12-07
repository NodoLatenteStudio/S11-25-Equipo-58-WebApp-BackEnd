package com.ecoshop.dto.Inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta de productos con stock bajo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBajoResponse {

    private Integer marcaId;
    private String nombreMarca;
    private Integer umbralStock; // Umbral configurado para considerar stock bajo
    private Integer totalProductosStockBajo;
    private List<ProductoStockBajo> productos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoStockBajo {
        private Integer productoId;
        private String nombre;
        private Integer stockActual;
        private Integer stockMinimoRecomendado;
        private String nivelAlerta; // "critico", "bajo", "medio"
        private String mensajeAlerta;
    }
}

