package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import com.portalcomunitario.msnotifications.messaging.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionListener.class);

    private final NotificacionDispatcher dispatcher;

    public NotificacionListener(NotificacionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onEvento(NotificacionEvento evento) {
        log.info("Evento recibido: tipo={}", evento != null ? evento.tipo() : "null");
        dispatcher.dispatch(evento);
    }
}
