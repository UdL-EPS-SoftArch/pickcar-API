package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.EmailToken;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.EmailTokenRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkSave;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RepositoryEventHandler
public class UserEventHandler {

    final Logger logger = LoggerFactory.getLogger(User.class);

    final UserRepository userRepository;
    final EmailTokenRepository emailTokenRepository;

    public UserEventHandler(UserRepository userRepository, EmailTokenRepository emailTokenRepository) {
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
    }

    @HandleBeforeCreate
    @Transactional
    public void handleUserPreCreate(User user) {
        logger.info("Before creating: {}", user.toString());
        // Self-registration always creates a driver pending email verification
        user.setRole(User.Role.DRIVER);
        user.setStatus(User.Status.PENDING_EMAIL);
        user.setEmailVerifiedAt(null);

        // Generate verification token
        String token = generateVerificationToken();
        user.setVerificationToken(token);
    }

    @HandleAfterCreate
    @Transactional
    public void handleUserPostCreate(User user) {
        logger.info("After creating: {}", user.toString());
        user.encodePassword();

        // Create verification token linked to the now-persisted user
        ZonedDateTime expiresAt = ZonedDateTime.now().plus(24, ChronoUnit.HOURS);
        EmailToken emailToken = new EmailToken();
        emailToken.setToken(user.getVerificationToken());
        emailToken.setHash(user.getVerificationToken());
        emailToken.setExpiresAt(expiresAt);
        emailToken.setType(EmailToken.TokenType.EMAIL_VERIFICATION);
        emailToken.setStatus(EmailToken.VerificationStatus.PENDING);
        emailToken.setUser(user);

        emailTokenRepository.save(emailToken);

        userRepository.save(user);
    }

    @HandleBeforeSave
    public void handleUserPreSave(User user) {
        logger.info("Before updating: {}", user.toString());
    }

    @HandleBeforeDelete
    public void handleUserPreDelete(User user) {
        logger.info("Before deleting: {}", user.toString());
    }

    @HandleBeforeLinkSave
    public void handleUserPreLinkSave(User user, Object o) {
        logger.info("Before linking: {} to {}", user.toString(), o.toString());
    }

    @HandleAfterSave
    public void handleUserPostSave(User user) {
        logger.info("After updating: {}", user.toString());
        if (user.isPasswordReset()) {
            user.encodePassword();
        }
        userRepository.save(user);
    }

    @HandleAfterDelete
    public void handleUserPostDelete(User user) {
        logger.info("After deleting: {}", user.toString());
    }

    @HandleAfterLinkSave
    public void handleUserPostLinkSave(User user, Object o) {
        logger.info("After linking: {} to {}", user.toString(), o.toString());
    }

    private String generateVerificationToken() {
        // TODO: replace with secure random token generator in production
        return "verify-" + System.currentTimeMillis();
    }
}
