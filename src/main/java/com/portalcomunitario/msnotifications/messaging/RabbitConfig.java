package com.portalcomunitario.msnotifications.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para ms-notifications (consumidor).
 * Declara el exchange de eventos del portal, la cola de notificaciones y su binding.
 * El servicio escucha TODOS los eventos de {@code portal.events} (routing key "#")
 * y decide qué hacer según el tipo del evento.
 */
@Configuration
public class RabbitConfig {

    /** Exchange topic común a todo el portal. */
    public static final String EXCHANGE = "portal.events";
    /** Cola única de la que consume ms-notifications. */
    public static final String QUEUE = "notifications.q";

    @Bean
    public TopicExchange portalEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange portalEventsExchange) {
        // "#" = todas las routing keys publicadas en el exchange.
        return BindingBuilder.bind(notificationsQueue).to(portalEventsExchange).with("#");
    }

    /** Mensajes en JSON (mismo converter en productores y consumidor). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
