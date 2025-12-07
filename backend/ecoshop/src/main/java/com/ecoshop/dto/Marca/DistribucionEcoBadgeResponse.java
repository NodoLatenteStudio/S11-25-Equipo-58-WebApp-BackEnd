package com.ecoshop.dto.Marca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la distribución de eco-badges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionEcoBadgeResponse {
    
    /**
     * Cantidad de productos con bajo impacto
     */
    private Integer bajoImpacto;
    
    /**
     * Cantidad de productos con medio impacto
     */
    private Integer medioImpacto;
    
    /**
     * Cantidad de productos con impacto neutro
     */
    private Integer neutro;
}

