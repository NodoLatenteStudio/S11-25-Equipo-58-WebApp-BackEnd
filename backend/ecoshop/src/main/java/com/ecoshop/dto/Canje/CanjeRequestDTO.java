package com.ecoshop.dto.Canje;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear canjes de recompensas.
 * 
 * Esta clase representa los datos necesarios para canjear una recompensa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanjeRequestDTO {

    /**
     * ID de la recompensa a canjear.
     */
    @NotNull(message = "El ID de la recompensa es obligatorio")
    private Integer recompensaId;
}
