package com.portalcomunitario.msnotifications.dto;

/**
 * Destinatario de una notificación, con su contacto y consentimiento.
 * En F3 estos datos los resuelve ms-notifications consultando a ms-auth por email.
 */
public record Destinatario(
        String nombre,
        String email,
        String telefono,
        boolean notificacionesActivas
) {
}
