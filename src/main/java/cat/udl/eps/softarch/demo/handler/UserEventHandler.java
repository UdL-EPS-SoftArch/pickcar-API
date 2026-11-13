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

import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.time.temporal.ChronoUnit;

@Component
@RepositoryEventHandler
public class UserEventHandler {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    final Logger logger = LoggerFactory.getLogger(User.class);

    final UserRepository userRepository;
    final EmailTokenRepository emailTokenRepository;

    final Validator validator;

    public UserEventHandler(UserRepository userRepository, EmailTokenRepository emailTokenRepository, Validator validator) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
    }

    @HandleBeforeCreate
    @Transactional
    public void handleUserPreCreate(User user) {
        logger.info("Before creating user");
        // Validate the plaintext password before it is replaced by its hash
        validatePlaintext(user);
        // Hash before persisting so plaintext never reaches the database
        user.encodePassword();
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
        logger.info("After creating user {}", user.getId());

        // Create verification token linked to the now-persisted user
        ZonedDateTime expiresAt = ZonedDateTime.now().plus(24, ChronoUnit.HOURS);
        EmailToken emailToken = new EmailToken();
        emailToken.setToken(user.getVerificationToken());
        emailToken.setHash(sha256Hex(user.getVerificationToken()));
        emailToken.setExpiresAt(expiresAt);
        emailToken.setType(EmailToken.TokenType.EMAIL_VERIFICATION);
        emailToken.setStatus(EmailToken.VerificationStatus.PENDING);
        emailToken.setUser(user);

        emailTokenRepository.save(emailToken);
    }

    @HandleBeforeSave
    public void handleUserPreSave(User user) {
        logger.info("Before updating user {}", user.getId());
        // Hash only if the password differs from the stored hash (i.e. it has been changed)
        String stored = user.getId() == null ? null : userRepository.findPasswordHashById(user.getId()).orElse(null);
        if (stored == null || !stored.equals(user.getPassword())) {
            validatePlaintext(user);
            user.encodePassword();
        }
    }

    @HandleBeforeDelete
    public void handleUserPreDelete(User user) {
        logger.info("Before deleting user {}", user.getId());
    }

    @HandleBeforeLinkSave
    public void handleUserPreLinkSave(User user, Object o) {
        logger.info("Before linking user {}", user.getId());
    }

    @HandleAfterSave
    public void handleUserPostSave(User user) {
        logger.info("After updating user {}", user.getId());
    }

    @HandleAfterDelete
    public void handleUserPostDelete(User user) {
        logger.info("After deleting user {}", user.getId());
    }

    @HandleAfterLinkSave
    public void handleUserPostLinkSave(User user, Object o) {
        logger.info("After linking user {}", user.getId());
    }

    private void validatePlaintext(User user) {
        Errors errors = new BeanPropertyBindingResult(user, "user");
        validator.validate(user, errors);
        if (errors.hasErrors()) {
            throw new RepositoryConstraintViolationException(errors);
        }
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
