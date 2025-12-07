package com.ecoshop.dto.Inventario;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta del historial de cambios de stock de un producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialStockResponse {

    private Integer productoId;
    private String nombreProducto;
    private Integer stockActual;
    private Integer totalRegistros;
    private List<RegistroStock> registros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroStock {
        private Integer stockHistorialId;
        private Integer stockAnterior;
        private Integer stockNuevo;
        private Integer cantidadCambio;
        private String tipoMovimiento;
        private String motivo;
        private Integer usuarioId;
        private Integer pedidoId;

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        private LocalDateTime fechaCambio;
    }
}

