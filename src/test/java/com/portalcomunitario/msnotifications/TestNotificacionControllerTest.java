package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import com.portalcomunitario.msnotifications.messaging.RabbitConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TestNotificacionControllerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TestNotificacionController controller;

    @Test
    @DisplayName("notificar: publica el evento en el exchange y routing key correctos y confirma en la respuesta")
    void notificar_publicaEnRabbit() {
        Destinatario d = new Destinatario("Juan", "juan@example.com", "+56912345678", true);
        NotificacionEvento evento = new NotificacionEvento("EVENTO_COMUNIDAD", "Titulo", "Mensaje", List.of(d));

        String respuesta = controller.notificar(evento);

        verify(rabbitTemplate, times(1))
                .convertAndSend(RabbitConfig.EXCHANGE, "evento.comunidad", evento);
        assertThat(respuesta).isEqualTo("Evento publicado en " + RabbitConfig.EXCHANGE);
    }
}
