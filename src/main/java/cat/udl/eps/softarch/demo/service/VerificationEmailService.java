package cat.udl.eps.softarch.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Sends the email-verification message (plain text containing the token) asynchronously. Failures are logged and never propagated
 * to the caller, so registration is not affected by mail problems.
 */
@Service
public class VerificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public VerificationEmailService(ObjectProvider<JavaMailSender> mailSender,
                                    @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    public void sendVerificationEmail(String to, String token, long expirationHours) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            logger.warn("No mail server configured (spring.mail.host); verification email to {} not sent", to);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Verify your PickCar email");
            helper.setText(buildBody(token, expirationHours), false);
            sender.send(message);
        } catch (Exception e) {
            logger.error("Could not send verification email to {}", to, e);
        }
    }

    String buildBody(String token, long expirationHours) {
        return "Welcome to PickCar!\n\n"
                + "Use the following token to verify your email address:\n\n"
                + token + "\n\n"
                + "This token expires in " + expirationHours + " hours.";
    }
}
