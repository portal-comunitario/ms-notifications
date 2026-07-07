package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.channel.NotificationChannel;
import com.portalcomunitario.msnotifications.dto.Destinatario;
import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reparte un evento de notificación a todos los canales habilitados,
 * respetando el consentimiento de cada destinatario.
 */
@Service
public class NotificacionDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificacionDispatcher.class);

    private final List<NotificationChannel> canales;

    public NotificacionDispatcher(List<NotificationChannel> canales) {
        this.canales = canales;
    }

    public void dispatch(NotificacionEvento evento) {
        if (evento == null || evento.destinatarios() == null || evento.destinatarios().isEmpty()) {
            log.warn("Evento sin destinatarios, se ignora: {}", evento != null ? evento.tipo() : "null");
            return;
        }

        int envios = 0;
        int omitidos = 0;
        for (Destinatario d : evento.destinatarios()) {
            if (d == null || !d.notificacionesActivas()) {
                omitidos++;
                continue; // respeta consentimiento
            }
            for (NotificationChannel canal : canales) {
                if (!canal.soporta(d)) {
                    continue;
                }
                try {
                    canal.enviar(d, evento.titulo(), evento.mensaje());
                    envios++;
                } catch (Exception ex) {
                    log.error("Fallo canal {} hacia {}: {}", canal.nombre(), d.email(), ex.getMessage());
                }
            }
        }
        log.info("Evento '{}' procesado: {} envíos, {} destinatarios omitidos (sin consentimiento)",
                evento.tipo(), envios, omitidos);
    }
}
