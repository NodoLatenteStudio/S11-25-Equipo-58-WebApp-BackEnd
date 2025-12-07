package com.ecoshop.dto.Inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para respuesta de predicción de demanda de un producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionDemandaResponse {

    private Integer productoId;
    private String nombreProducto;
    private Integer stockActual;
    private Integer stockMinimoRecomendado;
    private AnalisisDemanda analisis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisDemanda {
        private Integer ventasUltimoMes;
        private Integer ventasUltimos3Meses;
        private BigDecimal promedioVentasMensual;
        private BigDecimal promedioVentasSemanal;
        private Integer diasHastaAgotamiento; // Estimación basada en ventas promedio
        private String recomendacion; // "reponer", "monitorear", "suficiente"
        private Integer cantidadRecomendadaReposicion;
    }
}

