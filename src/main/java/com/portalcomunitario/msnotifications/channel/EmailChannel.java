package com.portalcomunitario.msnotifications.channel;

import com.portalcomunitario.msnotifications.dto.Destinatario;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Canal Email (Spring Mail, HTML). Si está deshabilitado, solo registra en el log (modo demo). */
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
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            if (from != null && !from.isBlank() && !from.startsWith("CAMBIAR")) {
                helper.setFrom(from, "Portal Comunitario");
            }
            helper.setTo(d.email());
            helper.setSubject(titulo);
            helper.setText(buildHtml(d.nombre(), titulo, mensaje), true);
            mailSender.send(mime);
            log.info("[email] enviado a {}", d.email());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String buildHtml(String nombre, String titulo, String mensaje) {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + esc(nombre) + "," : "Hola,";
        String cuerpo = esc(mensaje).replace("\n", "<br>");
        return """
            <div style="margin:0;padding:24px 12px;background:#f4f6f9;font-family:Arial,Helvetica,sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;">
                <tr>
                  <td style="background:#003087;padding:20px 24px;color:#ffffff;font-size:18px;font-weight:bold;">
                    🏘️ Portal Comunitario
                  </td>
                </tr>
                <tr>
                  <td style="padding:24px;color:#1f2937;">
                    <p style="margin:0 0 12px;color:#6b7280;font-size:14px;">%s</p>
                    <h1 style="margin:0 0 12px;font-size:20px;color:#003087;">%s</h1>
                    <p style="margin:0;font-size:15px;line-height:1.6;color:#374151;">%s</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding:16px 24px;background:#f9fafb;border-top:1px solid #eef2f7;color:#9ca3af;font-size:12px;">
                    Este es un mensaje automático de tu Portal Comunitario. Puedes gestionar tus notificaciones desde tu perfil.
                  </td>
                </tr>
              </table>
            </div>
            """.formatted(saludo, esc(titulo), cuerpo);
    }

    /** Escapa caracteres HTML para evitar inyección en el contenido. */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
