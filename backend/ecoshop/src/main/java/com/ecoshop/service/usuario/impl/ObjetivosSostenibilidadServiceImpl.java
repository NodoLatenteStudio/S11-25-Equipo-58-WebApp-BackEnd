package com.ecoshop.service.usuario.impl;

import com.ecoshop.domain.Usuario;
import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadRequest;
import com.ecoshop.dto.Usuario.ObjetivosSostenibilidadResponse;
import com.ecoshop.exception.ResourceNotFoundException;
import com.ecoshop.repository.usuario.UsuarioRepository;
import com.ecoshop.service.usuario.EcoPuntosService;
import com.ecoshop.service.impacto.ImpactoAmbientalService;
import com.ecoshop.service.usuario.ObjetivosSostenibilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementación del servicio para gestionar objetivos de sostenibilidad.
 * 
 * Permite a los usuarios establecer y seguir sus objetivos personalizados
 * de CO₂ ahorrado y eco-puntos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObjetivosSostenibilidadServiceImpl implements ObjetivosSostenibilidadService {

    private final UsuarioRepository usuarioRepository;
    private final ImpactoAmbientalService impactoAmbientalService;
    private final EcoPuntosService ecoPuntosService;

    // Valores por defecto para objetivos
    private static final BigDecimal META_CO2_DEFAULT = new BigDecimal("50.0");
    private static final Integer META_ECO_PUNTOS_DEFAULT = 500;

    @Override
    public ObjetivosSostenibilidadResponse obtenerObjetivos(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Obtener progreso actual
        var metricasUsuario = impactoAmbientalService.obtenerMetricasAmbientalesUsuario(usuarioId);
        var ecoPuntosResponse = ecoPuntosService.obtenerEcoPuntos(usuarioId);

        BigDecimal progresoCO2 = metricasUsuario.getCo2AhorradoTotal();
        Integer progresoEcoPuntos = ecoPuntosResponse.getEcoPuntos();

        // Obtener metas (usar valores por defecto si no están configurados)
        BigDecimal metaCO2 = usuario.getMetaCO2() != null ? usuario.getMetaCO2() : META_CO2_DEFAULT;
        Integer metaEcoPuntos = usuario.getMetaEcoPuntosPersonalizada() != null 
                ? usuario.getMetaEcoPuntosPersonalizada() 
                : META_ECO_PUNTOS_DEFAULT;

        return construirResponse(usuarioId, metaCO2, metaEcoPuntos, progresoCO2, progresoEcoPuntos);
    }

    @Override
    @Transactional
    public ObjetivosSostenibilidadResponse actualizarObjetivos(Integer usuarioId, ObjetivosSostenibilidadRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Actualizar metas
        if (request.getMetaCO2() != null) {
            usuario.setMetaCO2(request.getMetaCO2());
        }
        if (request.getMetaEcoPuntos() != null) {
            usuario.setMetaEcoPuntosPersonalizada(request.getMetaEcoPuntos());
        }

        usuarioRepository.save(usuario);

        // Obtener progreso actual después de actualizar
        return obtenerProgresoObjetivos(usuarioId);
    }

    @Override
    public ObjetivosSostenibilidadResponse obtenerProgresoObjetivos(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Obtener progreso actual
        var metricasUsuario = impactoAmbientalService.obtenerMetricasAmbientalesUsuario(usuarioId);
        var ecoPuntosResponse = ecoPuntosService.obtenerEcoPuntos(usuarioId);

        BigDecimal progresoCO2 = metricasUsuario.getCo2AhorradoTotal();
        Integer progresoEcoPuntos = ecoPuntosResponse.getEcoPuntos();

        // Obtener metas (usar valores por defecto si no están configurados)
        BigDecimal metaCO2 = usuario.getMetaCO2() != null ? usuario.getMetaCO2() : META_CO2_DEFAULT;
        Integer metaEcoPuntos = usuario.getMetaEcoPuntosPersonalizada() != null 
                ? usuario.getMetaEcoPuntosPersonalizada() 
                : META_ECO_PUNTOS_DEFAULT;

        return construirResponse(usuarioId, metaCO2, metaEcoPuntos, progresoCO2, progresoEcoPuntos);
    }

    /**
     * Construye la respuesta con todos los cálculos de progreso.
     */
    private ObjetivosSostenibilidadResponse construirResponse(
            Integer usuarioId,
            BigDecimal metaCO2,
            Integer metaEcoPuntos,
            BigDecimal progresoCO2,
            Integer progresoEcoPuntos) {

        // Calcular porcentajes y valores restantes para CO₂
        BigDecimal porcentajeProgresoCO2 = BigDecimal.ZERO;
        BigDecimal co2Restante = metaCO2;
        Boolean objetivoCO2Alcanzado = false;

        if (metaCO2.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeProgresoCO2 = progresoCO2
                    .divide(metaCO2, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
            
            co2Restante = metaCO2.subtract(progresoCO2);
            if (co2Restante.compareTo(BigDecimal.ZERO) < 0) {
                co2Restante = BigDecimal.ZERO;
            }
            
            objetivoCO2Alcanzado = progresoCO2.compareTo(metaCO2) >= 0;
        }

        // Calcular porcentajes y valores restantes para Eco-Puntos
        Double porcentajeProgresoEcoPuntos = 0.0;
        Integer ecoPuntosRestantes = metaEcoPuntos;
        Boolean objetivoEcoPuntosAlcanzado = false;

        if (metaEcoPuntos > 0) {
            porcentajeProgresoEcoPuntos = (progresoEcoPuntos.doubleValue() / metaEcoPuntos.doubleValue()) * 100.0;
            porcentajeProgresoEcoPuntos = Math.round(porcentajeProgresoEcoPuntos * 100.0) / 100.0;
            
            ecoPuntosRestantes = metaEcoPuntos - progresoEcoPuntos;
            if (ecoPuntosRestantes < 0) {
                ecoPuntosRestantes = 0;
            }
            
            objetivoEcoPuntosAlcanzado = progresoEcoPuntos >= metaEcoPuntos;
        }

        // Generar mensaje motivacional
        String mensajeMotivacional = generarMensajeMotivacional(
                objetivoCO2Alcanzado,
                objetivoEcoPuntosAlcanzado,
                porcentajeProgresoCO2,
                porcentajeProgresoEcoPuntos);

        return ObjetivosSostenibilidadResponse.builder()
                .usuarioId(usuarioId)
                .metaCO2(metaCO2)
                .progresoCO2(progresoCO2.setScale(2, RoundingMode.HALF_UP))
                .porcentajeProgresoCO2(porcentajeProgresoCO2)
                .co2Restante(co2Restante.setScale(2, RoundingMode.HALF_UP))
                .metaEcoPuntos(metaEcoPuntos)
                .progresoEcoPuntos(progresoEcoPuntos)
                .porcentajeProgresoEcoPuntos(porcentajeProgresoEcoPuntos)
                .ecoPuntosRestantes(ecoPuntosRestantes)
                .objetivoCO2Alcanzado(objetivoCO2Alcanzado)
                .objetivoEcoPuntosAlcanzado(objetivoEcoPuntosAlcanzado)
                .mensajeMotivacional(mensajeMotivacional)
                .build();
    }

    /**
     * Genera un mensaje motivacional basado en el progreso del usuario.
     */
    private String generarMensajeMotivacional(
            Boolean objetivoCO2Alcanzado,
            Boolean objetivoEcoPuntosAlcanzado,
            BigDecimal porcentajeCO2,
            Double porcentajeEcoPuntos) {

        // Si ambos objetivos están alcanzados
        if (objetivoCO2Alcanzado && objetivoEcoPuntosAlcanzado) {
            return "¡Felicidades! Has alcanzado todos tus objetivos de sostenibilidad. ¡Sigue así! 🌱";
        }

        // Si solo CO₂ está alcanzado
        if (objetivoCO2Alcanzado) {
            return "¡Excelente! Has alcanzado tu objetivo de CO₂. Continúa trabajando en tus eco-puntos. 💚";
        }

        // Si solo eco-puntos está alcanzado
        if (objetivoEcoPuntosAlcanzado) {
            return "¡Genial! Has alcanzado tu objetivo de eco-puntos. Sigue reduciendo tu huella de carbono. 🌍";
        }

        // Determinar cuál objetivo está más cerca
        double porcentajeMayor = Math.max(porcentajeCO2.doubleValue(), porcentajeEcoPuntos);

        if (porcentajeMayor >= 80) {
            return "¡Estás muy cerca! Sigue así, estás a punto de alcanzar tus objetivos. 💪";
        } else if (porcentajeMayor >= 50) {
            return "Vas por buen camino. Ya has completado más de la mitad de tus objetivos. 🌟";
        } else if (porcentajeMayor >= 25) {
            return "Buen progreso. Cada compra sostenible te acerca más a tus objetivos. 🌱";
        } else {
            return "¡Comienza tu viaje hacia la sostenibilidad! Cada paso cuenta. 🌍";
        }
    }
}

