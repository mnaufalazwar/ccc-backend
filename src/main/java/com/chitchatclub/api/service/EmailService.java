package com.chitchatclub.api.service;

import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int TOKEN_EXPIRY_HOURS = 48;

    private final JavaMailSender mailSender;
    private final VerificationTokenService tokenService;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender, VerificationTokenService tokenService) {
        this.mailSender = mailSender;
        this.tokenService = tokenService;
    }

    public void sendVerificationEmail(User user) {
        String token = tokenService.createToken(user.getId());

        String verifyLink = frontendUrl + "/verify-email?token=" + token;

        String html = """
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 2rem;">
              <h2 style="color: #1B2A4A; margin-bottom: 0.5rem;">Welcome to ChitChatClub!</h2>
              <p style="color: #475569; line-height: 1.7;">
                Hi <strong>%s</strong>, thanks for signing up. Please verify your email address to start joining English conversation sessions.
              </p>
              <div style="text-align: center; margin: 2rem 0;">
                <a href="%s"
                   style="display: inline-block; background: #2563eb; color: #fff; padding: 0.75rem 2rem; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 1rem;">
                  Verify My Email
                </a>
              </div>
              <p style="color: #94a3b8; font-size: 0.85rem; line-height: 1.6;">
                If the button doesn't work, copy and paste this link into your browser:<br>
                <a href="%s" style="color: #2563eb; word-break: break-all;">%s</a>
              </p>
              <p style="color: #94a3b8; font-size: 0.85rem;">This link expires in %d hours.</p>
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 2rem 0;">
              <p style="color: #94a3b8; font-size: 0.8rem;">If you didn't create an account with ChitChatClub, you can safely ignore this email.</p>
            </div>
            """.formatted(user.getFullName(), verifyLink, verifyLink, verifyLink, TOKEN_EXPIRY_HOURS);

        sendEmail(user.getEmail(), "Verify your email — ChitChatClub", html);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String html = """
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 2rem;">
              <h2 style="color: #1B2A4A; margin-bottom: 0.5rem;">Reset Your Password</h2>
              <p style="color: #475569; line-height: 1.7;">
                Hi <strong>%s</strong>, we received a request to reset your password. Click the button below to choose a new password.
              </p>
              <div style="text-align: center; margin: 2rem 0;">
                <a href="%s"
                   style="display: inline-block; background: #2563eb; color: #fff; padding: 0.75rem 2rem; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 1rem;">
                  Reset Password
                </a>
              </div>
              <p style="color: #94a3b8; font-size: 0.85rem; line-height: 1.6;">
                If the button doesn't work, copy and paste this link into your browser:<br>
                <a href="%s" style="color: #2563eb; word-break: break-all;">%s</a>
              </p>
              <p style="color: #94a3b8; font-size: 0.85rem;">This link expires in 1 hour.</p>
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 2rem 0;">
              <p style="color: #94a3b8; font-size: 0.8rem;">If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
            </div>
            """.formatted(user.getFullName(), resetLink, resetLink, resetLink);

        sendEmail(user.getEmail(), "Reset your password — ChitChatClub", html);
    }

    public String buildSessionEmailDefaultSubject(Session session) {
        String date = session.getStartDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mm a"));
        return "Reminder: " + session.getTitle() + " — " + date;
    }

    public String buildSessionEmailDefaultBody(Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi there!\n\n");
        sb.append("This is a friendly reminder about your upcoming session:\n\n");
        sb.append("📌 ").append(session.getTitle()).append("\n");
        sb.append("📅 ").append(session.getStartDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"))).append("\n");
        sb.append("⏱ ").append(session.getDurationMinutes()).append(" minutes\n");

        if (session.getZoomLink() != null && !session.getZoomLink().isBlank()) {
            sb.append("\n--- Zoom Meeting Details ---\n");
            sb.append("🔗 Link: ").append(session.getZoomLink()).append("\n");
            if (session.getZoomMeetingId() != null && !session.getZoomMeetingId().isBlank()) {
                sb.append("🆔 Meeting ID: ").append(session.getZoomMeetingId()).append("\n");
            }
            if (session.getZoomPassword() != null && !session.getZoomPassword().isBlank()) {
                sb.append("🔑 Password: ").append(session.getZoomPassword()).append("\n");
            }
        }

        sb.append("\nSee you there!\n— ChitChatClub Team");
        return sb.toString();
    }

    public int sendSessionEmail(Session session, List<User> recipients, String subject, String body) {
        String htmlBody = buildSessionEmailHtml(body, session);
        int sent = 0;
        for (User recipient : recipients) {
            sendEmail(recipient.getEmail(), subject, htmlBody);
            sent++;
        }
        log.info("Session email blast for '{}': sent to {} recipients", session.getTitle(), sent);
        return sent;
    }

    private String buildSessionEmailHtml(String plainTextBody, Session session) {
        String escapedBody = plainTextBody
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        return """
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 2rem;">
              <h2 style="color: #1B2A4A; margin-bottom: 0.5rem;">%s</h2>
              <div style="color: #475569; line-height: 1.7;">
                %s
              </div>
              %s
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 2rem 0;">
              <p style="color: #94a3b8; font-size: 0.8rem;">You're receiving this because you're registered for this session on ChitChatClub.</p>
            </div>
            """.formatted(
                session.getTitle(),
                escapedBody,
                session.getZoomLink() != null && !session.getZoomLink().isBlank()
                    ? """
                      <div style="text-align: center; margin: 2rem 0;">
                        <a href="%s"
                           style="display: inline-block; background: #2563eb; color: #fff; padding: 0.75rem 2rem; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 1rem;">
                          Join Zoom Meeting
                        </a>
                      </div>
                      """.formatted(session.getZoomLink())
                    : ""
        );
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
