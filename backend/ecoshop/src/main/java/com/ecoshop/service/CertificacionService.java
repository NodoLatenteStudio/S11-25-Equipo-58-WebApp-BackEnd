package com.ecoshop.service;

import com.ecoshop.dto.CertificacionRequestDTO;
import com.ecoshop.dto.CertificacionResponseDTO;

import java.util.List;

/**
 * Interfaz del servicio de certificaciones.
 * 
 * Define el contrato para las operaciones de negocio relacionadas con certificaciones.
 * Esta interfaz separa la definición de las operaciones de su implementación,
 * permitiendo cambiar la implementación sin afectar a los clientes (controladores).
 * 
 * Responsabilidades:
 * - Definir las operaciones CRUD (Create, Read, Update, Delete) para certificaciones
 * - Trabajar con DTOs en lugar de entidades para mantener desacoplamiento
 * - Manejar la lógica de negocio relacionada con certificaciones
 * 
 * ¿Por qué usar una interfaz de servicio?
 * - Separación de responsabilidades: define qué se hace, no cómo se hace
 * - Facilita el testing: permite crear implementaciones mock para pruebas
 * - Flexibilidad: permite cambiar la implementación sin afectar a los clientes
 * - Documentación: sirve como contrato claro de las operaciones disponibles
 * 
 * Operaciones disponibles:
 * - createCertificacion: Crea una nueva certificación
 * - getCertificacionById: Obtiene una certificación por su ID
 * - getAllCertificaciones: Obtiene todas las certificaciones
 * - getCertificacionByNombreSello: Obtiene una certificación por nombre de sello
 * - updateCertificacion: Actualiza una certificación existente
 * - deleteCertificacion: Elimina una certificación
 */
public interface CertificacionService {
    
    /**
     * Crea una nueva certificación en la base de datos.
     * 
     * Valida que el nombre del sello sea único antes de crear la certificación.
     * 
     * @param dto Datos de la certificación a crear
     * @return CertificacionResponseDTO con la certificación creada y su ID asignado
     * @throws com.ecoshop.exception.BadRequestException si ya existe una certificación con el mismo nombre de sello
     */
    CertificacionResponseDTO createCertificacion(CertificacionRequestDTO dto);
    
    /**
     * Obtiene una certificación por su ID.
     * 
     * @param id Identificador único de la certificación
     * @return CertificacionResponseDTO de la certificación encontrada
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     */
    CertificacionResponseDTO getCertificacionById(Integer id);
    
    /**
     * Obtiene todas las certificaciones existentes.
     * 
     * @return Lista de todas las certificaciones convertidas a CertificacionResponseDTO
     */
    List<CertificacionResponseDTO> getAllCertificaciones();
    
    /**
     * Obtiene una certificación por su nombre de sello.
     * 
     * La búsqueda es case-insensitive para mayor flexibilidad.
     * 
     * @param nombreSello Nombre del sello de certificación
     * @return CertificacionResponseDTO de la certificación encontrada
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     */
    CertificacionResponseDTO getCertificacionByNombreSello(String nombreSello);
    
    /**
     * Actualiza una certificación existente.
     * 
     * Valida que el nombre del sello sea único (si se cambia) antes de actualizar.
     * 
     * @param id Identificador de la certificación a actualizar
     * @param dto Nuevos datos de la certificación
     * @return CertificacionResponseDTO actualizado
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     * @throws com.ecoshop.exception.BadRequestException si el nuevo nombre de sello ya existe en otra certificación
     */
    CertificacionResponseDTO updateCertificacion(Integer id, CertificacionRequestDTO dto);
    
    /**
     * Elimina una certificación de la base de datos.
     * 
     * Elimina primero las relaciones con productos de la tabla intermedia y luego la certificación.
     * 
     * @param id Identificador de la certificación a eliminar
     * @throws com.ecoshop.exception.ResourceNotFoundException si la certificación no existe
     */
    void deleteCertificacion(Integer id);
}

