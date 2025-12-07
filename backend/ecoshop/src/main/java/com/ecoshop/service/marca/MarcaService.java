package com.ecoshop.service.marca;

import com.ecoshop.dto.Marca.MarcaRequestDTO;
import com.ecoshop.dto.Marca.MarcaResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar marcas.
 * 
 * Esta interfaz define los métodos de negocio para operaciones CRUD sobre marcas.
 * La implementación se encuentra en MarcaServiceImpl.
 * 
 * Responsabilidades:
 * - Crear nuevas marcas
 * - Obtener marcas por ID o todas las marcas
 * - Actualizar marcas existentes
 * - Eliminar marcas
 * 
 * Todas las operaciones de escritura están dentro de transacciones para
 * garantizar la integridad de los datos.
 */
public interface MarcaService {

    /**
     * Crea una nueva marca en la base de datos.
     * 
     * Proceso:
     * 1. Valida que el usuario exista
     * 2. Convierte el DTO a entidad usando el mapper
     * 3. Asigna la relación con Usuario
     * 4. Guarda la marca en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO con el ID asignado
     * 
     * @param dto MarcaRequestDTO con los datos de la marca a crear
     * @return MarcaResponseDTO con la marca creada y su ID asignado
     * @throws com.ecoshop.exception.ResourceNotFoundException si el usuario no existe
     * 
     * Ejemplo de uso:
     * POST /api/v1/marcas
     * Body: { "usuarioId": 1, "nombreOficial": "EcoLife", ... }
     */
    MarcaResponseDTO createMarca(MarcaRequestDTO dto);

    /**
     * Busca una marca por su ID.
     * 
     * Proceso:
     * 1. Busca la marca en la BD
     * 2. Si no existe, lanza una excepción
     * 3. Si existe, la convierte a MarcaResponseDTO usando el mapper
     * 4. Retorna el DTO
     * 
     * @param id Identificador de la marca
     * @return MarcaResponseDTO de la marca encontrada
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * GET /api/v1/marcas/1
     */
    MarcaResponseDTO getMarcaById(Integer id);

    /**
     * Obtiene todas las marcas de la base de datos.
     * 
     * Proceso:
     * 1. Obtiene todas las entidades Marca de la BD
     * 2. Convierte cada entidad a MarcaResponseDTO usando el mapper
     * 3. Retorna la lista de DTOs
     * 
     * @return Lista de todas las marcas convertidas a MarcaResponseDTO
     * 
     * Ejemplo de uso:
     * GET /api/v1/marcas
     */
    List<MarcaResponseDTO> getAllMarcas();

    /**
     * Actualiza una marca existente.
     * 
     * Proceso:
     * 1. Verifica que la marca exista
     * 2. Preserva la fecha de unión original
     * 3. Actualiza los campos básicos usando el mapper
     * 4. Guarda los cambios en la BD
     * 5. Convierte la entidad a DTO de respuesta usando el mapper
     * 6. Retorna el DTO actualizado
     * 
     * @param id ID de la marca a actualizar
     * @param dto Nuevos datos de la marca
     * @return MarcaResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * PUT /api/v1/marcas/1
     * Body: { "nombreOficial": "EcoLife Actualizado", ... }
     */
    MarcaResponseDTO updateMarca(Integer id, MarcaRequestDTO dto);

    /**
     * Elimina una marca de la base de datos.
     * 
     * Proceso:
     * 1. Verifica que la marca exista
     * 2. Elimina la marca de la BD
     * 
     * Nota: Si la marca tiene productos asociados, la eliminación puede fallar
     * debido a restricciones de integridad referencial, a menos que se maneje
     * con CASCADE DELETE o eliminando primero los productos.
     * 
     * @param id Identificador de la marca a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si la marca no existe
     * 
     * Ejemplo de uso:
     * DELETE /api/v1/marcas/1
     */
    void deleteMarca(Integer id);
    
    /**
     * Busca marcas por nombre oficial con paginación (búsqueda parcial, case-insensitive).
     * 
     * @param query Término de búsqueda (nombre oficial o parte del nombre)
     * @param page Número de página (basado en 1, por defecto 1)
     * @param size Tamaño de la página (por defecto 6)
     * @return MarcaPaginadoResponse con las marcas que coinciden con el nombre, paginadas
     */
    com.ecoshop.dto.Marca.MarcaPaginadoResponse buscarMarcasPorNombre(String query, Integer page, Integer size);
}
