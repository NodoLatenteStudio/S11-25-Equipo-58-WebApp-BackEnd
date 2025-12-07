package com.ecoshop.controller.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para endpoints de salud y estado de la aplicación.
 * 
 * Este controlador proporciona endpoints útiles para monitoreo y verificación
 * del estado de la API. Es comúnmente usado por herramientas de orquestación
 * como Kubernetes, Docker Swarm, etc., para verificar si la aplicación está
 * funcionando correctamente.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * Endpoint de health check para verificar que la API está funcionando.
     * 
     * Este endpoint se usa para:
     * - Verificar que la aplicación está corriendo
     * - Monitoreo de salud de la aplicación
     * - Load balancers para determinar si la instancia está disponible
     * 
     * @return Mensaje de estado de la API
     * 
     * Ejemplo de uso:
     * GET http://localhost:8080/api/v1/health
     * Respuesta: "EcoShop API OK"
     */
    @GetMapping("/health")
    public String health() {
        return "EcoShop API OK";
    }
}

