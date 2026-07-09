package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Canal WhatsApp vía la API REST de Twilio (sin SDK, usando RestClient).
 * Si está deshabilitado o sin credenciales, solo registra en el log (modo demo).
 */
@Component
public class WhatsAppChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppChannel.class);

    private final boolean enabled;
    private final String accountSid;
    private final String authToken;
    private final String from;
    private final RestClient http = RestClient.create();

    public WhatsAppChannel(@Value("${app.notifications.whatsapp.enabled:false}") boolean enabled,
                           @Value("${app.twilio.account-sid:}") String accountSid,
                           @Value("${app.twilio.auth-token:}") String authToken,
                           @Value("${app.twilio.whatsapp-from:}") String from) {
        this.enabled = enabled;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.from = from;
    }

    @Override
    public String nombre() {
        return "whatsapp";
    }

    @Override
    public boolean soporta(Destinatario d) {
        return d.telefono() != null && !d.telefono().isBlank();
    }

    @Override
    public void enviar(Destinatario d, String titulo, String mensaje) {
        String to = normalizar(d.telefono());
        // Formato WhatsApp: *negrita* para el título, cuerpo, y firma en _cursiva_.
        StringBuilder sb = new StringBuilder();
        if (titulo != null && !titulo.isBlank()) sb.append("🔔 *").append(titulo).append("*\n\n");
        if (mensaje != null && !mensaje.isBlank()) sb.append(mensaje).append("\n\n");
        sb.append("_Portal Comunitario_");
        String cuerpo = sb.toString();

        boolean sinCredenciales = accountSid == null || accountSid.isBlank() || accountSid.startsWith("CAMBIAR");
        if (!enabled || sinCredenciales) {
            log.info("[whatsapp:log] Para={} · {}", to, cuerpo.replaceAll("\\s+", " "));
            return;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", from);
        form.add("To", to);
        form.add("Body", cuerpo);

        http.post()
                .uri(url)
                .header("Authorization", basicAuth(accountSid, authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        log.info("[whatsapp] enviado a {}", to);
    }

    /** Devuelve el número en formato Twilio WhatsApp: "whatsapp:+56...". */
    private String normalizar(String telefono) {
        String t = telefono.trim();
        if (t.startsWith("whatsapp:")) {
            return t;
        }
        if (!t.startsWith("+")) {
            t = "+" + t.replaceAll("[^0-9]", "");
        }
        return "whatsapp:" + t;
    }

    private String basicAuth(String user, String pass) {
        String creds = user + ":" + (pass != null ? pass : "");
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }
}
