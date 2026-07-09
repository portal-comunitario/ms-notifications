package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailChannelTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailChannel channel;

    @BeforeEach
    void setUp() {
        // enabled=false (modo log / demo): no debe usarse el JavaMailSender.
        channel = new EmailChannel(mailSender, false, "portal@example.com");
    }

    private Destinatario conEmail(String email) {
        return new Destinatario("Juan", email, "+56912345678", true);
    }

    private Destinatario conEmailYNombre(String email, String nombre) {
        return new Destinatario(nombre, email, "+56912345678", true);
    }

    private MimeMessage nuevoMime() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    @DisplayName("nombre: identifica el canal como 'email'")
    void nombre_esEmail() {
        assertThat(channel.nombre()).isEqualTo("email");
    }

    @Test
    @DisplayName("soporta: true cuando hay email no vacio, false cuando es nulo o en blanco")
    void soporta_dependeDelEmail() {
        assertThat(channel.soporta(conEmail("juan@example.com"))).isTrue();
        assertThat(channel.soporta(conEmail(null))).isFalse();
        assertThat(channel.soporta(conEmail("   "))).isFalse();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza y NO usa el JavaMailSender")
    void enviar_modoLog_noUsaMailSender() {
        assertThatCode(() -> channel.enviar(conEmail("juan@example.com"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("enviar (modo log): no lanza cuando titulo y mensaje son nulos y no toca el mailSender")
    void enviar_modoLog_valoresNulos_noLanza() {
        assertThatCode(() -> channel.enviar(conEmail("juan@example.com"), null, null))
                .doesNotThrowAnyException();

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("enviar (enabled): arma el MimeMessage con from/to/asunto y llama send()")
    void enviar_enabled_armaMimeYEnvia() throws Exception {
        EmailChannel enabled = new EmailChannel(mailSender, true, "portal@example.com");
        MimeMessage mime = nuevoMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);

        enabled.enviar(conEmail("juan@example.com"), "Bienvenido", "Tu cuenta esta activa");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mime);
        assertThat(mime.getSubject()).isEqualTo("Bienvenido");
        assertThat(mime.getAllRecipients()).hasSize(1);
        assertThat(mime.getAllRecipients()[0].toString()).isEqualTo("juan@example.com");
        assertThat(mime.getFrom()[0].toString()).contains("portal@example.com");
    }

    @Test
    @DisplayName("enviar (enabled): buildHtml/esc escapan caracteres HTML en nombre, titulo y mensaje")
    void enviar_enabled_escapaHtml() throws Exception {
        EmailChannel enabled = new EmailChannel(mailSender, true, "portal@example.com");
        MimeMessage mime = nuevoMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);

        enabled.enviar(conEmailYNombre("juan@example.com", "Ana & <b>"),
                "Aviso <x>", "1 < 2 & \"ok\"\nfin");

        verify(mailSender, times(1)).send(mime);
        String html = (String) mime.getContent();
        assertThat(html).contains("Hola Ana &amp; &lt;b&gt;,");
        assertThat(html).contains("Aviso &lt;x&gt;");
        assertThat(html).contains("1 &lt; 2 &amp; &quot;ok&quot;<br>fin");
        // No debe filtrar los caracteres crudos sin escapar en el cuerpo.
        assertThat(html).doesNotContain("<b>");
    }

    @Test
    @DisplayName("enviar (enabled): con 'from' en blanco no fija remitente pero igual envia")
    void enviar_enabled_fromEnBlanco_noFijaRemitente() throws Exception {
        EmailChannel enabled = new EmailChannel(mailSender, true, "");
        MimeMessage mime = nuevoMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);

        enabled.enviar(conEmail("juan@example.com"), "Titulo", "Mensaje");

        verify(mailSender, times(1)).send(mime);
        assertThat(mime.getFrom()).isNull();
    }

    @Test
    @DisplayName("enviar (enabled): con 'from' placeholder (CAMBIAR...) no fija remitente pero igual envia")
    void enviar_enabled_fromPlaceholder_noFijaRemitente() throws Exception {
        EmailChannel enabled = new EmailChannel(mailSender, true, "CAMBIAR_ME@example.com");
        MimeMessage mime = nuevoMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);

        enabled.enviar(conEmail("juan@example.com"), "Titulo", "Mensaje");

        verify(mailSender, times(1)).send(mime);
        assertThat(mime.getFrom()).isNull();
    }

    @Test
    @DisplayName("enviar (enabled): un fallo del mailSender se envuelve en RuntimeException")
    void enviar_enabled_fallo_envuelveExcepcion() {
        EmailChannel enabled = new EmailChannel(mailSender, true, "portal@example.com");
        MimeMessage mime = nuevoMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doThrow(new MailSendException("SMTP caido")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> enabled.enviar(conEmail("juan@example.com"), "Titulo", "Mensaje"))
                .isInstanceOf(RuntimeException.class);
    }
}
