package com.ecoshop.dto.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear o actualizar categorías.
 * 
 * Esta clase representa los datos de una categoría que se transfieren desde
 * el cliente al servidor en operaciones de creación (POST) o actualización (PUT).
 * 
 * Validaciones:
 * Las anotaciones de validación (@NotBlank, @Size, etc.) se
 * activan automáticamente cuando se usa @Valid en el controlador.
 * 
 * Campos:
 * - nombre: Nombre de la categoría (obligatorio, máximo 100 caracteres)
 * - descripcion: Descripción de la categoría (opcional)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequestDTO {

    /**
     * Nombre de la categoría.
     * 
     * @NotBlank: Este campo es obligatorio y no puede estar vacío
     * @Size: Longitud máxima de 100 caracteres
     * 
     * Ejemplos: "Ropa", "Hogar", "Accesorios", "Cuidado Personal"
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder 100 caracteres")
    private String nombre;

    /**
     * Descripción de la categoría.
     * 
     * Campo opcional que describe qué tipo de productos pertenecen a esta categoría.
     */
    private String descripcion;
}

