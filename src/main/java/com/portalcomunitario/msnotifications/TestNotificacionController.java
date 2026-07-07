package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import com.portalcomunitario.msnotifications.messaging.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de prueba (dev): publica un evento en RabbitMQ para validar el flujo
 * consumidor → canales sin necesidad de los productores. Quitar/asegurar en prod.
 */
@RestController
@RequestMapping("/test")
public class TestNotificacionController {

    private final RabbitTemplate rabbitTemplate;

    public TestNotificacionController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/notificar")
    public String notificar(@RequestBody NotificacionEvento evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "evento.comunidad", evento);
        return "Evento publicado en " + RabbitConfig.EXCHANGE;
    }
}
