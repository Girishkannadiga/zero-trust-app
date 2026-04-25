package com.zerotrust.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendLoginNotificationEmail(String toEmail, String name, String ip,
                                            String location, String device, String time) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Login Detected — Zero Trust");
            String body = """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;background:#0d1117;color:#e6edf3;padding:32px;border-radius:12px;border:1px solid #30363d">
                  <h2 style="color:#2f81f7">&#128274; New Login Detected</h2>
                  <p>Hello <strong>%s</strong>, a new login to your account was recorded.</p>
                  <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                    <tr><td style="padding:8px;color:#8b949e;border-bottom:1px solid #30363d">IP Address</td><td style="padding:8px;border-bottom:1px solid #30363d;font-family:monospace">%s</td></tr>
                    <tr><td style="padding:8px;color:#8b949e;border-bottom:1px solid #30363d">Location</td><td style="padding:8px;border-bottom:1px solid #30363d">%s</td></tr>
                    <tr><td style="padding:8px;color:#8b949e;border-bottom:1px solid #30363d">Device</td><td style="padding:8px;border-bottom:1px solid #30363d">%s</td></tr>
                    <tr><td style="padding:8px;color:#8b949e">Time</td><td style="padding:8px">%s</td></tr>
                  </table>
                  <p style="color:#f85149;font-size:0.85rem">If this was not you, contact your administrator immediately and change your password.</p>
                </div>
                """.formatted(name, ip, location, device != null ? device : "Unknown", time);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send login notification to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Zero Trust Verification Code");
            helper.setText(buildEmailBody(name, otp), true);
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmailBody(String name, String otp) {
        return """
               <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;background:#0d1117;color:#e6edf3;padding:32px;border-radius:12px;border:1px solid #30363d">
                 <h2 style="color:#2f81f7;margin-bottom:4px">&#128274; Zero Trust Security</h2>
                 <p style="color:#8b949e;font-size:0.8rem;margin-top:0">Secure Cloud Access System</p>
                 <hr style="border:none;border-top:1px solid #30363d;margin:20px 0"/>
                 <p>Hello <strong>%s</strong>,</p>
                 <p>Your one-time verification code is:</p>
                 <div style="background:#161b22;border:1px solid #30363d;border-radius:8px;padding:24px;text-align:center;margin:20px 0">
                   <span style="font-size:2.8rem;font-weight:900;letter-spacing:14px;color:#2f81f7;font-family:monospace">%s</span>
                 </div>
                 <p style="color:#8b949e;font-size:0.85rem">
                   This code expires in <strong style="color:#e6edf3">10 minutes</strong>.<br/>
                   Never share this code with anyone.
                 </p>
                 <hr style="border:none;border-top:1px solid #30363d;margin:20px 0"/>
                 <p style="color:#8b949e;font-size:0.75rem;margin:0">
                   If you did not attempt to log in, please contact your administrator immediately.
                 </p>
               </div>
               """.formatted(name, otp);
    }
}
