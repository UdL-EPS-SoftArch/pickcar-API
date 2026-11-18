package cat.udl.eps.softarch.demo.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VerificationEmailServiceTest {

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> provider(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(sender);
        return p;
    }

    @Test
    void sendsRenderedEmail() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);
        VerificationEmailService service = new VerificationEmailService(provider(sender), "from@pickcar.app");

        service.sendVerificationEmail("user@sample.app", "abc123", 24);

        verify(sender).send(message);
        assertEquals("user@sample.app", message.getAllRecipients()[0].toString());
        assertEquals("Verify your PickCar email", message.getSubject());
    }

    @Test
    void bodyContainsTokenAndExpiration() {
        VerificationEmailService service = new VerificationEmailService(provider(null), "f@x");
        String body = service.buildBody("abc123", 24);
        assertTrue(body.contains("abc123"));
        assertTrue(body.contains("24 hours"));
    }

    @Test
    void doesNothingWithoutMailServerOrOnFailure() {
        new VerificationEmailService(provider(null), "f@x").sendVerificationEmail("a@b.c", "t", 24);

        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenThrow(new RuntimeException("boom"));
        new VerificationEmailService(provider(sender), "f@x").sendVerificationEmail("a@b.c", "t", 24);
        verify(sender, never()).send(any(MimeMessage.class));
    }
}
