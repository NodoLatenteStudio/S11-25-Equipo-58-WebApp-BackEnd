package com.ecoshop.service.categoria;

import com.ecoshop.dto.Categoria.CategoriaRequestDTO;
import com.ecoshop.dto.Categoria.CategoriaResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar categorías.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre categorías.
 * La implementación se encuentra en CategoriaServiceImpl.
 * 
 * Responsabilidades:
 * - Crear nuevas categorías
 * - Obtener categorías por ID o todas las categorías
 * - Actualizar categorías existentes
 * - Eliminar categorías
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 */
public interface CategoriaService {

    /**
     * Crea una nueva categoría en la base de datos.
     * 
     * @param dto CategoriaRequestDTO con los datos de la categoría a crear
     * @return CategoriaResponseDTO con la categoría creada y su ID asignado
     * @throws com.ecoshop.exception.BadRequestException si ya existe una categoría con el mismo nombre
     */
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO dto);

    /**
     * Busca una categoría por su ID.
     * 
     * @param id Identificador de la categoría
     * @return CategoriaResponseDTO de la categoría encontrada
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     */
    CategoriaResponseDTO getCategoriaById(Integer id);

    /**
     * Obtiene todas las categorías de la base de datos.
     * 
     * @return Lista de todas las categorías convertidas a CategoriaResponseDTO
     */
    List<CategoriaResponseDTO> getAllCategorias();

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría a actualizar
     * @param dto Nuevos datos de la categoría
     * @return CategoriaResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     * @throws com.ecoshop.exception.BadRequestException si el nuevo nombre ya existe en otra categoría
     */
    CategoriaResponseDTO updateCategoria(Integer id, CategoriaRequestDTO dto);

    /**
     * Elimina una categoría de la base de datos.
     * 
     * @param id Identificador de la categoría a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si la categoría no existe
     */
    void deleteCategoria(Integer id);
}

