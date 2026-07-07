package com.portalcomunitario.msnotifications.dto;

import java.util.List;

/**
 * Envelope de un evento de notificación que llega por RabbitMQ.
 * Lo publican ms-community y ms-auth; ms-notifications lo consume y hace fan-out.
 *
 * @param tipo          tipo lógico (CUOTA_PENDIENTE, EVENTO_COMUNIDAD, etc.) — para logging/plantillas
 * @param titulo        asunto/encabezado del mensaje
 * @param mensaje       cuerpo del mensaje ya redactado (texto plano)
 * @param destinatarios a quién notificar (con su contacto y consentimiento ya resueltos)
 */
public record NotificacionEvento(
        String tipo,
        String titulo,
        String mensaje,
        List<Destinatario> destinatarios
) {
}
