package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Canal Email (Spring Mail). Si está deshabilitado, solo registra en el log (modo demo). */
@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailChannel(JavaMailSender mailSender,
                        @Value("${app.notifications.email.enabled:false}") boolean enabled,
                        @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    @Override
    public String nombre() {
        return "email";
    }

    @Override
    public boolean soporta(Destinatario d) {
        return d.email() != null && !d.email().isBlank();
    }

    @Override
    public void enviar(Destinatario d, String titulo, String mensaje) {
        if (!enabled) {
            log.info("[email:log] Para={} · Asunto='{}' · {}", d.email(), titulo, mensaje);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        if (from != null && !from.isBlank() && !from.startsWith("CAMBIAR")) {
            msg.setFrom(from);
        }
        msg.setTo(d.email());
        msg.setSubject(titulo);
        msg.setText(mensaje);
        mailSender.send(msg);
        log.info("[email] enviado a {}", d.email());
    }
}
