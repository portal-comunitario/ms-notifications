package com.portalcomunitario.msnotifications;

import com.portalcomunitario.msnotifications.channel.NotificationChannel;
import com.portalcomunitario.msnotifications.dto.Destinatario;
import com.portalcomunitario.msnotifications.dto.NotificacionEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionDispatcherTest {

    @Mock
    private NotificationChannel canal;

    private NotificacionDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new NotificacionDispatcher(List.of(canal));
    }

    private Destinatario destinatario(boolean notificacionesActivas) {
        return new Destinatario("Juan", "juan@example.com", "+56912345678", notificacionesActivas);
    }

    private NotificacionEvento evento(Destinatario... destinatarios) {
        return new NotificacionEvento("EVENTO_COMUNIDAD", "Titulo", "Mensaje",
                new ArrayList<>(Arrays.asList(destinatarios)));
    }

    @Test
    @DisplayName("dispatch: evento nulo no falla y no toca ningun canal")
    void dispatch_eventoNulo_noFalla() {
        assertThatCode(() -> dispatcher.dispatch(null)).doesNotThrowAnyException();
        verify(canal, never()).enviar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: evento con lista de destinatarios nula no falla y no envia")
    void dispatch_destinatariosNulos_noFalla() {
        NotificacionEvento ev = new NotificacionEvento("TIPO", "T", "M", null);
        assertThatCode(() -> dispatcher.dispatch(ev)).doesNotThrowAnyException();
        verify(canal, never()).enviar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: evento sin destinatarios (lista vacia) no falla y no envia")
    void dispatch_sinDestinatarios_noFalla() {
        NotificacionEvento ev = new NotificacionEvento("TIPO", "T", "M", List.of());
        assertThatCode(() -> dispatcher.dispatch(ev)).doesNotThrowAnyException();
        verify(canal, never()).enviar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: ignora destinatarios sin consentimiento (notificacionesActivas=false)")
    void dispatch_sinConsentimiento_noEnvia() {
        dispatcher.dispatch(evento(destinatario(false)));
        // se omite antes de llegar al canal: soporta() ni siquiera se consulta
        verify(canal, never()).soporta(any());
        verify(canal, never()).enviar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: con consentimiento y canal que soporta, invoca enviar() una vez con titulo y mensaje")
    void dispatch_conConsentimientoYSoportado_envia() {
        Destinatario d = destinatario(true);
        when(canal.soporta(d)).thenReturn(true);

        dispatcher.dispatch(evento(d));

        verify(canal, times(1)).enviar(eq(d), eq("Titulo"), eq("Mensaje"));
    }

    @Test
    @DisplayName("dispatch: con consentimiento pero canal que NO soporta, no invoca enviar()")
    void dispatch_conConsentimientoNoSoportado_noEnvia() {
        Destinatario d = destinatario(true);
        when(canal.soporta(d)).thenReturn(false);

        dispatcher.dispatch(evento(d));

        verify(canal, never()).enviar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: mezcla — solo el destinatario con consentimiento y soportado recibe envio")
    void dispatch_mezcla_soloEnviaAlActivoSoportado() {
        Destinatario activo = destinatario(true);
        Destinatario inactivo = destinatario(false);
        when(canal.soporta(activo)).thenReturn(true);

        dispatcher.dispatch(evento(inactivo, activo));

        verify(canal, times(1)).enviar(eq(activo), anyString(), anyString());
        verify(canal, never()).enviar(eq(inactivo), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: destinatario nulo dentro de la lista se ignora sin fallar")
    void dispatch_destinatarioNuloEnLista_seIgnora() {
        Destinatario activo = destinatario(true);
        when(canal.soporta(activo)).thenReturn(true);

        assertThatCode(() -> dispatcher.dispatch(evento(null, activo)))
                .doesNotThrowAnyException();

        verify(canal, times(1)).enviar(eq(activo), anyString(), anyString());
    }

    @Test
    @DisplayName("dispatch: una excepcion del canal se captura y no se propaga")
    void dispatch_canalLanzaExcepcion_noPropaga() {
        Destinatario d = destinatario(true);
        when(canal.soporta(d)).thenReturn(true);
        doThrow(new RuntimeException("boom")).when(canal).enviar(any(), anyString(), anyString());

        assertThatCode(() -> dispatcher.dispatch(evento(d))).doesNotThrowAnyException();

        verify(canal, times(1)).enviar(eq(d), anyString(), anyString());
    }
}
