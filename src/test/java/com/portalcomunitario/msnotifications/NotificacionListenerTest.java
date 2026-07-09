package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacionListenerTest {

    @Mock
    private NotificacionDispatcher dispatcher;

    @InjectMocks
    private NotificacionListener listener;

    @Test
    @DisplayName("onEvento: delega el evento recibido en el dispatcher exactamente una vez")
    void onEvento_delegaEnDispatcher() {
        Destinatario d = new Destinatario("Juan", "juan@example.com", "+56912345678", true);
        NotificacionEvento evento = new NotificacionEvento("EVENTO_COMUNIDAD", "Titulo", "Mensaje", List.of(d));

        listener.onEvento(evento);

        verify(dispatcher, times(1)).dispatch(evento);
    }

    @Test
    @DisplayName("onEvento: un evento nulo no rompe (log usa 'null') y aun asi delega en el dispatcher")
    void onEvento_eventoNulo_noRompe() {
        assertThatCode(() -> listener.onEvento(null)).doesNotThrowAnyException();

        verify(dispatcher, times(1)).dispatch(null);
    }
}
