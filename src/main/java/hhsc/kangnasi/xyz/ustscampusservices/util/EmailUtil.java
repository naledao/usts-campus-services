package hhsc.kangnasi.xyz.ustscampusservices.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility component for sending emails via Spring's JavaMailSender.
 * Supports simple text, HTML, and attachments.
 */
@Component
public class EmailUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

    private final JavaMailSender mailSender;

    /**
     * Default sender address. Falls back to spring.mail.username when app.mail.from is not set.
     */
    @Value("${app.mail.from:${spring.mail.username:}}")
    private String defaultFrom;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ---------------- Simple Text ----------------

    public void sendText(String to, String subject, String text) {
        sendText(null, to, subject, text);
    }

    public void sendText(@Nullable String from, String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        msg.setFrom(resolveFrom(from));
        mailSender.send(msg);
        log.debug("Sent text email to {} with subject '{}'", to, subject);
    }

    // ---------------- HTML ----------------

    public void sendHtml(String to, String subject, String html) {
        sendHtml(null, to, subject, html);
    }

    public void sendHtml(@Nullable String from, String to, String subject, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(resolveFrom(from));
            mailSender.send(mimeMessage);
            log.debug("Sent HTML email to {} with subject '{}'", to, subject);
        } catch (MessagingException e) {
            throw new MailSendRuntimeException("Failed to send HTML email", e);
        }
    }

    // ---------------- Attachments (text or HTML) ----------------

    public void sendWithAttachments(String to, String subject, String content, boolean isHtml, File... attachments) {
        sendWithAttachments(null, to, subject, content, isHtml, attachments);
    }

    public void sendWithAttachments(@Nullable String from, String to, String subject, String content, boolean isHtml, File... attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            helper.setFrom(resolveFrom(from));

            if (attachments != null) {
                for (File file : attachments) {
                    if (file == null) continue;
                    if (!file.exists() || !file.isFile()) {
                        log.warn("Attachment not found or not a file: {}", file);
                        continue;
                    }
                    FileSystemResource resource = new FileSystemResource(file);
                    helper.addAttachment(Objects.requireNonNullElse(resource.getFilename(), file.getName()), resource);
                }
            }

            mailSender.send(mimeMessage);
            log.debug("Sent email with attachments to {} with subject '{}'", to, subject);
        } catch (MessagingException e) {
            throw new MailSendRuntimeException("Failed to send email with attachments", e);
        }
    }

    private String resolveFrom(@Nullable String from) {
        String value = (from != null && !from.isBlank()) ? from : defaultFrom;
        if (value == null || value.isBlank()) {
            log.warn("No 'from' address configured. Consider setting app.mail.from or spring.mail.username.");
        }
        return value;
    }

    public static class MailSendRuntimeException extends RuntimeException {
        public MailSendRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

