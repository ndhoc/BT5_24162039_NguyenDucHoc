package com.nguyenduchoc.bt5.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;
import com.nguyenduchoc.bt5.entity.OtpToken;
import com.nguyenduchoc.bt5.entity.ProfileEmailChange;

/**
 * Gửi email OTP qua SMTP. Khi chưa cấu hình spring.mail.host, OTP được ghi vào
 * <catalina.base>/logs/otp-dev.log để tiện kiểm thử cục bộ.
 */
@Component("otpMailSender")
public final class MailSender {
    private final Properties config = new Properties();
    private final Session session;

    public MailSender(Environment environment) {
        config.setProperty("mail.smtp.host", environment.getProperty("spring.mail.host", ""));
        config.setProperty("mail.smtp.port", environment.getProperty("spring.mail.port", "587"));
        config.setProperty("mail.smtp.username", environment.getProperty("spring.mail.username", ""));
        config.setProperty("mail.smtp.password", environment.getProperty("spring.mail.password", ""));
        config.setProperty("mail.smtp.from", environment.getProperty("app.mail.from", "no-reply@shop24162039.local"));
        config.setProperty("mail.smtp.auth", environment.getProperty("spring.mail.properties.mail.smtp.auth", "true"));
        config.setProperty("mail.smtp.starttls", environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "true"));
        config.setProperty("mail.smtp.timeout", environment.getProperty("spring.mail.properties.mail.smtp.timeout", "10000"));
        config.setProperty("mail.smtp.connectiontimeout", environment.getProperty("spring.mail.properties.mail.smtp.connectiontimeout", "10000"));
        session = createSession();
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        props.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls"));
        props.put("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.connectiontimeout", config.getProperty("mail.smtp.connectiontimeout"));
        props.put("mail.smtp.timeout", config.getProperty("mail.smtp.timeout"));
        props.put("mail.mime.charset", "UTF-8");
        String username = config.getProperty("mail.smtp.username");
        String password = config.getProperty("mail.smtp.password");
        if (Boolean.parseBoolean(config.getProperty("mail.smtp.auth"))) {
            return Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        return Session.getInstance(props);
    }

    public boolean isConfigured() {
        return !config.getProperty("mail.smtp.host").isBlank();
    }

    public void sendOtp(String to, String fullName, String code, String purpose) {
        String subject = switch (purpose) {
            case OtpToken.PURPOSE_ACTIVATION -> "Kích hoạt tài khoản Shop 24162039";
            case ProfileEmailChange.PURPOSE -> "Xác nhận email mới Shop 24162039";
            default -> "Đặt lại mật khẩu Shop 24162039";
        };
        String action = switch (purpose) {
            case OtpToken.PURPOSE_ACTIVATION -> "kích hoạt tài khoản";
            case ProfileEmailChange.PURPOSE -> "xác nhận email mới";
            default -> "đặt lại mật khẩu";
        };
        if (!isConfigured()) {
            writeDevLog(to, code, purpose);
            return;
        }
        try {
            MimeMessage message = new MimeMessage(session);
            String from = config.getProperty("mail.smtp.from");
            message.setFrom(new InternetAddress(from, "Shop 24162039", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, "UTF-8");
            MimeMultipart body = new MimeMultipart("alternative");
            MimeBodyPart plain = new MimeBodyPart();
            plain.setText(text(fullName, code, action), "UTF-8");
            MimeBodyPart rich = new MimeBodyPart();
            rich.setContent(html(fullName, code, action), "text/html; charset=UTF-8");
            body.addBodyPart(plain);
            body.addBodyPart(rich);
            message.setContent(body);
            Transport.send(message);
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Không gửi được email OTP. Vui lòng thử lại.", e);
        }
    }

    private static String text(String fullName, String code, String action) {
        return "Xin chào " + fullName + ",\n\n"
                + "Mã OTP để " + action + " của bạn là: " + code + "\n"
                + "Mã có hiệu lực trong 10 phút và chỉ dùng được một lần.\n\n"
                + "Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email.\n"
                + "Shop 24162039";
    }

    private static String html(String fullName, String code, String action) {
        return "<div style=\"font-family:Segoe UI,Arial,sans-serif;color:#1e293b\">"
                + "<h2 style=\"color:#1d4ed8\">Shop 24162039</h2>"
                + "<p>Xin chào <strong>" + escape(fullName) + "</strong>,</p>"
                + "<p>Mã OTP để " + action + " của bạn là:</p>"
                + "<p style=\"font-size:2rem;font-weight:800;letter-spacing:.35em;color:#0f172a\">" + code + "</p>"
                + "<p>Mã có hiệu lực trong <strong>10 phút</strong> và chỉ dùng được một lần.</p>"
                + "<p style=\"color:#64748b\">Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email.</p>"
                + "</div>";
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void writeDevLog(String to, String code, String purpose) {
        String line = LocalDateTime.now() + " | " + purpose + " | " + to + " | " + code;
        System.out.println("[OTP-DEV] " + line);
        try {
            String base = System.getProperty("catalina.base");
            Path directory = base != null && !base.isBlank()
                    ? Path.of(base, "logs")
                    : Path.of(System.getProperty("java.io.tmpdir"), "shop24162039-logs");
            Files.createDirectories(directory);
            try (OutputStream out = Files.newOutputStream(directory.resolve("otp-dev.log"),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                 PrintWriter writer = new PrintWriter(out)) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("[OTP-DEV] Không ghi được otp-dev.log: " + e.getMessage());
        }
    }
}
