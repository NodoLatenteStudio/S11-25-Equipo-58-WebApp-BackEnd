package com.ecoshop.dto.clerk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para recibir webhooks de Clerk.
 * 
 * Clerk envía webhooks cuando ocurren eventos relacionados con usuarios:
 * - user.created: Cuando se crea un usuario en Clerk
 * - user.updated: Cuando se actualiza un usuario en Clerk
 * - user.deleted: Cuando se elimina un usuario en Clerk
 * 
 * Estructura del webhook de Clerk:
 * {
 *   "object": "event",
 *   "type": "user.created",
 *   "data": {
 *     "id": "user_xxxxx",
 *     "email_addresses": [...],
 *     "first_name": "Juan",
 *     "last_name": "Pérez",
 *     ...
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClerkWebhookDTO {

    /**
     * Tipo de objeto (siempre "event" para webhooks).
     */
    private String object;

    /**
     * Tipo de evento (user.created, user.updated, user.deleted, etc.).
     */
    private String type;

    /**
     * Datos del evento. Contiene información del usuario.
     */
    private Map<String, Object> data;

    /**
     * ID del usuario en Clerk (clerkId).
     * Se extrae de data.id.
     */
    @JsonProperty("data")
    public void setData(Map<String, Object> data) {
        this.data = data;
        // Extraer el ID del usuario si está disponible
        if (data != null && data.containsKey("id")) {
            this.clerkId = (String) data.get("id");
        }
    }

    /**
     * ID del usuario en Clerk (clerkId).
     * Se extrae automáticamente de data.id.
     */
    private String clerkId;

    /**
     * Obtiene el email del usuario desde los datos del webhook.
     * 
     * @return El primer email del usuario, o null si no está disponible
     */
    public String getEmail() {
        if (data == null) {
            return null;
        }

        // Clerk almacena los emails en un array email_addresses
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> emailAddresses = 
                (java.util.List<Map<String, Object>>) data.get("email_addresses");

        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            Map<String, Object> firstEmail = emailAddresses.get(0);
            return (String) firstEmail.get("email_address");
        }

        return null;
    }

    /**
     * Obtiene el nombre completo del usuario desde los datos del webhook.
     * 
     * @return El nombre completo (first_name + last_name), o null si no está disponible
     */
    public String getFullName() {
        if (data == null) {
            return null;
        }

        String firstName = (String) data.get("first_name");
        String lastName = (String) data.get("last_name");

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }

        return null;
    }
}

