package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class WhatsAppChannelTest {

    private WhatsAppChannel channel;

    @BeforeEach
    void setUp() {
        // enabled=false (modo log / demo): no se hace ninguna llamada HTTP real a Twilio.
        channel = new WhatsAppChannel(false, "", "", "");
    }

    private Destinatario conTelefono(String telefono) {
        return new Destinatario("Juan", "juan@example.com", telefono, true);
    }

    @Test
    @DisplayName("nombre: identifica el canal como 'whatsapp'")
    void nombre_esWhatsapp() {
        assertThat(channel.nombre()).isEqualTo("whatsapp");
    }

    @Test
    @DisplayName("soporta: true cuando hay telefono no vacio, false cuando es nulo o en blanco")
    void soporta_dependeDelTelefono() {
        assertThat(channel.soporta(conTelefono("+56912345678"))).isTrue();
        assertThat(channel.soporta(conTelefono(null))).isFalse();
        assertThat(channel.soporta(conTelefono("   "))).isFalse();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza para telefono ya en formato internacional (+)")
    void enviar_formatoInternacional_noLanza() {
        assertThatCode(() -> channel.enviar(conTelefono("+56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza para telefono sin prefijo +")
    void enviar_sinPrefijo_noLanza() {
        assertThatCode(() -> channel.enviar(conTelefono("56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza para telefono ya en formato whatsapp:")
    void enviar_formatoWhatsapp_noLanza() {
        assertThatCode(() -> channel.enviar(conTelefono("whatsapp:+56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza para telefono con separadores y con espacios")
    void enviar_conSeparadores_noLanza() {
        assertThatCode(() -> channel.enviar(conTelefono(" 9 1234-5678 "), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (modo log): no lanza cuando titulo y mensaje son nulos")
    void enviar_tituloYMensajeNulos_noLanza() {
        assertThatCode(() -> channel.enviar(conTelefono("+56912345678"), null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (enabled pero SID nulo): sin credenciales cae a modo log, no lanza y no hace HTTP")
    void enviar_enabledSidNulo_modoLog_noLanza() {
        WhatsAppChannel sinSid = new WhatsAppChannel(true, null, "token", "whatsapp:+123");
        assertThatCode(() -> sinSid.enviar(conTelefono("+56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (enabled pero SID en blanco): sin credenciales cae a modo log, no lanza y no hace HTTP")
    void enviar_enabledSidEnBlanco_modoLog_noLanza() {
        WhatsAppChannel sinSid = new WhatsAppChannel(true, "   ", "token", "whatsapp:+123");
        assertThatCode(() -> sinSid.enviar(conTelefono("+56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviar (enabled pero SID placeholder CAMBIAR...): sin credenciales cae a modo log, no lanza y no hace HTTP")
    void enviar_enabledSidPlaceholder_modoLog_noLanza() {
        WhatsAppChannel sinSid = new WhatsAppChannel(true, "CAMBIAR_SID", "token", "whatsapp:+123");
        assertThatCode(() -> sinSid.enviar(conTelefono("+56912345678"), "Titulo", "Mensaje"))
                .doesNotThrowAnyException();
    }
}
