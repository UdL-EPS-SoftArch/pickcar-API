package cat.udl.eps.softarch.demo.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.domain.EmailToken;
import cat.udl.eps.softarch.demo.domain.EmailToken.VerificationStatus;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import cat.udl.eps.softarch.demo.repository.EmailTokenRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestContextManager;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.SpringBootContextLoader;

import java.time.ZonedDateTime;

@ContextConfiguration(
    classes = {cat.udl.eps.softarch.demo.DemoApplication.class},
    loader = SpringBootContextLoader.class
)
@WebAppConfiguration
public class VerifyEmailStepDefs {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailTokenRepository emailTokenRepository;

    @Autowired
    private cat.udl.eps.softarch.demo.service.EmailVerificationService emailVerificationService;

    private String verificationToken;

    @Given("There is a registered user with email {string}")
    public void thereIsARegisteredUserWithEmail(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setPassword("password");
            u.encodePassword();
            u.setRole(User.Role.DRIVER);
            u.setStatus(User.Status.PENDING_EMAIL);
            // Generate verification token like UserEventHandler does
            String token = "verify-" + System.currentTimeMillis();
            u.setVerificationToken(token);
            // Create EmailToken like UserEventHandler.handleUserPostCreate does
            ZonedDateTime expiresAt = ZonedDateTime.now().plus(24, java.time.temporal.ChronoUnit.HOURS);
            EmailToken emailToken = new EmailToken();
            emailToken.setToken(token);
            emailToken.setHash(token);
            emailToken.setExpiresAt(expiresAt);
            emailToken.setType(EmailToken.TokenType.EMAIL_VERIFICATION);
            emailToken.setStatus(EmailToken.VerificationStatus.PENDING);
            User saved = userRepository.save(u);
            emailToken.setUser(saved);
            emailTokenRepository.save(emailToken);
            return saved;
        });
        verificationToken = user.getVerificationToken();
    }

    @Given("The user has a pending verification token")
    public void theUserHasAPendingVerificationToken() {
        assertNotNull(verificationToken, "Verification token should be set");
    }

    @Given("The user has a pending verification token that has expired")
    public void theUserHasAPendingVerificationTokenThatHasExpired() {
        EmailToken token = emailTokenRepository.findByToken(verificationToken);
        assertNotNull(token, "Verification token should exist");
        token.setExpiresAt(ZonedDateTime.now().minusHours(1));
        emailTokenRepository.save(token);
    }

    @When("I verify the email with a valid token")
    public void iVerifyTheEmailWithAValidToken() {
        emailVerificationService.verifyToken(verificationToken);
    }

    @When("I verify the email with an expired token")
    @When("I verify the email with the expired token")
    public void iVerifyTheEmailWithAnExpiredToken() {
        emailVerificationService.verifyToken(verificationToken);
    }

    @When("I verify the email with an invalid token")
    public void iVerifyTheEmailWithAnInvalidToken() {
        emailVerificationService.verifyToken("does-not-exist");
    }

    @Then("The user status should be ACTIVE")
    public void theUserStatusShouldBeACTIVE() {
        User user = userRepository.findByEmail("user@sample.app").orElseThrow();
        assertEquals(User.Status.ACTIVE, user.getStatus());
    }

    @Then("The email should be verified at the current time")
    public void theEmailShouldBeVerifiedAtTheCurrentTime() {
        User user = userRepository.findByEmail("user@sample.app").orElseThrow();
        assertNotNull(user.getEmailVerifiedAt());
    }

    @Then("The verification token should be marked as VERIFIED")
    public void theVerificationTokenShouldBeMarkedAsVERIFIED() {
        EmailToken token = emailTokenRepository.findByToken(verificationToken);
        assertNotNull(token);
        assertEquals(VerificationStatus.VERIFIED, token.getStatus());
    }

    @Then("The verification token should be marked as EXPIRED")
    public void theVerificationTokenShouldBeMarkedAsEXPIRED() {
        EmailToken token = emailTokenRepository.findByToken(verificationToken);
        assertNotNull(token);
        assertEquals(VerificationStatus.EXPIRED, token.getStatus());
    }

    @Then("The user status should remain PENDING_EMAIL")
    public void theUserStatusShouldRemainPENDING_EMAIL() {
        User user = userRepository.findByEmail("user@sample.app").orElseThrow();
        assertEquals(User.Status.PENDING_EMAIL, user.getStatus());
    }
}