package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;

/**
 * Canal de envío de notificaciones (email, WhatsApp, …).
 * Permite sumar canales nuevos sin tocar el dispatcher.
 */
public interface NotificationChannel {

    /** Nombre corto del canal (para logs). */
    String nombre();

    /** true si este canal puede alcanzar al destinatario (tiene el dato de contacto necesario). */
    boolean soporta(Destinatario destinatario);

    /** Envía el mensaje. Debe lanzar excepción si falla (el dispatcher la captura y registra). */
    void enviar(Destinatario destinatario, String titulo, String mensaje);
}
