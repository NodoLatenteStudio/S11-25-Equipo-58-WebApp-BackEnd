package com.ecoshop.service.usuario;

import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadRequest;
import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadResponse;

/**
 * Interfaz del servicio para gestionar objetivos de sostenibilidad.
 * 
 * Este servicio permite a los usuarios establecer y seguir sus objetivos
 * de sostenibilidad personalizados.
 */
public interface ObjetivosSostenibilidadService {

    /**
     * Obtiene los objetivos de sostenibilidad del usuario con su progreso actual.
     * 
     * @param usuarioId ID del usuario
     * @return ObjetivosSostenibilidadResponse con objetivos y progreso
     */
    ObjetivosSostenibilidadResponse obtenerObjetivos(Integer usuarioId);

    /**
     * Actualiza los objetivos de sostenibilidad del usuario.
     * 
     * @param usuarioId ID del usuario
     * @param request Objetivos a actualizar
     * @return ObjetivosSostenibilidadResponse actualizado
     */
    ObjetivosSostenibilidadResponse actualizarObjetivos(Integer usuarioId, ObjetivosSostenibilidadRequest request);

    /**
     * Obtiene el progreso detallado hacia los objetivos.
     * 
     * @param usuarioId ID del usuario
     * @return ObjetivosSostenibilidadResponse con progreso detallado
     */
    ObjetivosSostenibilidadResponse obtenerProgresoObjetivos(Integer usuarioId);
}

