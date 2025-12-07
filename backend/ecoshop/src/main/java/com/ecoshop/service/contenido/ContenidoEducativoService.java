package com.ecoshop.service.contenido;

import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoRequestDTO;
import com.ecoshop.dto.ContenidoEducativo.ContenidoEducativoResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio para gestionar contenido educativo.
 */
public interface ContenidoEducativoService {
    
    /**
     * Crea un nuevo contenido educativo.
     * 
     * @param dto Datos del contenido educativo a crear
     * @return Contenido educativo creado
     */
    ContenidoEducativoResponseDTO createContenidoEducativo(ContenidoEducativoRequestDTO dto);
    
    /**
     * Obtiene un contenido educativo por su ID.
     * 
     * @param id ID del contenido educativo
     * @return Contenido educativo encontrado
     */
    ContenidoEducativoResponseDTO getContenidoEducativoById(Integer id);
    
    /**
     * Obtiene todos los contenidos educativos.
     * 
     * @return Lista de contenidos educativos
     */
    List<ContenidoEducativoResponseDTO> getAllContenidosEducativos();
    
    /**
     * Obtiene todos los contenidos educativos activos.
     * 
     * @return Lista de contenidos educativos activos
     */
    List<ContenidoEducativoResponseDTO> getContenidosEducativosActivos();
    
    /**
     * Obtiene contenidos educativos por categoría.
     * 
     * @param categoria Categoría del contenido
     * @return Lista de contenidos educativos de la categoría
     */
    List<ContenidoEducativoResponseDTO> getContenidosEducativosByCategoria(String categoria);
    
    /**
     * Busca contenidos educativos por título.
     * 
     * @param titulo Título o parte del título a buscar
     * @return Lista de contenidos educativos que coinciden
     */
    List<ContenidoEducativoResponseDTO> buscarContenidosEducativosPorTitulo(String titulo);
    
    /**
     * Actualiza un contenido educativo existente.
     * 
     * @param id ID del contenido educativo a actualizar
     * @param dto Nuevos datos del contenido educativo
     * @return Contenido educativo actualizado
     */
    ContenidoEducativoResponseDTO updateContenidoEducativo(Integer id, ContenidoEducativoRequestDTO dto);
    
    /**
     * Elimina un contenido educativo.
     * 
     * @param id ID del contenido educativo a eliminar
     */
    void deleteContenidoEducativo(Integer id);
}

