package com.ecoshop.dto.Categoria;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para respuestas de categorías.
 * 
 * Esta clase representa los datos de una categoría que se transfieren desde
 * el servidor al cliente en operaciones de consulta (GET).
 * 
 * Campos:
 * - categoriaId: Identificador único de la categoría
 * - nombre: Nombre de la categoría
 * - descripcion: Descripción de la categoría
 * - fechaCreacion: Fecha en que se creó la categoría
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {

    private Integer categoriaId;
    private String nombre;
    private String descripcion;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;
}

