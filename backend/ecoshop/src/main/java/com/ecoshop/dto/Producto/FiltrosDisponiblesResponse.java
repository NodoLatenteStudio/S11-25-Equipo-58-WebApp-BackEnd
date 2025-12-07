package com.ecoshop.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) para las opciones disponibles de filtros.
 * 
 * Esta clase representa las opciones disponibles para cada tipo de filtro,
 * útil para poblar dropdowns y checkboxes en el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltrosDisponiblesResponse {

    /**
     * Lista de categorías disponibles con su ID y nombre.
     */
    private List<CategoriaFiltro> categorias;

    /**
     * Lista de niveles de impacto disponibles.
     */
    private List<String> nivelesImpacto;

    /**
     * Lista de marcas disponibles con su ID y nombre.
     */
    private List<MarcaFiltro> marcas;

    /**
     * Lista de materiales únicos disponibles.
     */
    private List<String> materiales;

    /**
     * Lista de orígenes únicos disponibles.
     */
    private List<String> origenes;

    /**
     * Lista de certificaciones disponibles con su ID y nombre.
     */
    private List<CertificacionFiltro> certificaciones;

    /**
     * Rango de precios disponible (mínimo y máximo).
     */
    private RangoPrecio rangoPrecio;

    /**
     * Opciones de ordenamiento disponibles.
     */
    private List<String> opcionesOrdenamiento;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaFiltro {
        private Integer categoriaId;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcaFiltro {
        private Integer marcaId;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificacionFiltro {
        private Integer certificacionId;
        private String nombreSello;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangoPrecio {
        private java.math.BigDecimal precioMin;
        private java.math.BigDecimal precioMax;
    }
}

