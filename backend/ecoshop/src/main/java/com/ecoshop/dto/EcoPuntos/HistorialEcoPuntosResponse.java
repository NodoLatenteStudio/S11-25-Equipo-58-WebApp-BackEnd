package com.ecoshop.dto.EcoPuntos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para el historial de eco-puntos de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEcoPuntosResponse {
    
    private Integer usuarioId;
    private Integer totalPuntos;
    private List<RegistroPuntos> registros;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroPuntos {
        private Integer pedidoId;
        
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        private LocalDateTime fecha;
        
        private Integer puntosGanados;
        private String motivo; // Ej: "Compra sostenible", "Productos con bajo impacto", etc.
        private String descripcion; // Detalle de cómo se ganaron los puntos
    }
}

