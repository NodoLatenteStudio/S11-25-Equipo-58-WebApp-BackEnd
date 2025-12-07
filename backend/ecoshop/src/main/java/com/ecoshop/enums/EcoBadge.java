package com.ecoshop.enums;

/**
 * Enum que representa los niveles de impacto ambiental (Eco-Badges) de un producto.
 * 
 * Estos badges se calculan automáticamente basándose en:
 * - Huella de carbono total
 * - Porcentaje de material reciclable
 * - Distancia de transporte
 * - Tipo y cantidad de certificaciones
 * 
 * Valores posibles:
 * - BAJO_IMPACTO: Producto con bajo impacto ambiental (verde) 🌱
 * - MEDIO_IMPACTO: Producto con impacto ambiental moderado (naranja) 🟠
 * - NEUTRO: Producto con impacto neutral (azul) 🔵
 */
public enum EcoBadge {
    
    /**
     * Bajo impacto ambiental.
     * 
     * Productos más sostenibles con:
     * - Huella de carbono baja
     * - Alto porcentaje de material reciclable
     * - Origen local o cercano
     * - Múltiples certificaciones ambientales
     */
    BAJO_IMPACTO("bajo_impacto", "Bajo impacto"),
    
    /**
     * Impacto ambiental moderado.
     * 
     * Productos con:
     * - Huella de carbono moderada
     * - Porcentaje medio de material reciclable
     * - Origen intermedio
     * - Algunas certificaciones
     */
    MEDIO_IMPACTO("medio_impacto", "Medio impacto"),
    
    /**
     * Impacto neutral.
     * 
     * Productos con:
     * - Huella de carbono estándar
     * - Bajo porcentaje de material reciclable
     * - Origen distante
     * - Pocas o ninguna certificación
     */
    NEUTRO("neutro", "Neutro");
    
    private final String valor;
    private final String descripcion;
    
    EcoBadge(String valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene el valor del badge (usado en la base de datos).
     * 
     * @return Valor del badge (ej: "bajo_impacto")
     */
    public String getValor() {
        return valor;
    }
    
    /**
     * Obtiene la descripción legible del badge.
     * 
     * @return Descripción del badge (ej: "Bajo impacto")
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Convierte un string a EcoBadge.
     * 
     * @param valor Valor del badge (ej: "bajo_impacto")
     * @return EcoBadge correspondiente, o null si no se encuentra
     */
    public static EcoBadge fromValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        
        String valorNormalizado = valor.trim().toLowerCase();
        for (EcoBadge badge : values()) {
            if (badge.valor.equals(valorNormalizado)) {
                return badge;
            }
        }
        return null;
    }
    
    /**
     * Verifica si un string es un valor válido de EcoBadge.
     * 
     * @param valor Valor a verificar
     * @return true si es válido, false en caso contrario
     */
    public static boolean esValido(String valor) {
        return fromValor(valor) != null;
    }
}

