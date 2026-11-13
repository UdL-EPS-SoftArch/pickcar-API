package cat.udl.eps.softarch.demo.service;

import cat.udl.eps.softarch.demo.domain.EmailToken;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.EmailTokenRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * Service in charge of processing email verification tokens.
 * <p>
 * When a verification token is provided:
 * - If the token exists, is not expired, and has status PENDING:
 *   - Updates the associated user's status to ACTIVE
 *   - Sets the user's emailVerifiedAt to the current time
 *   - Updates the email token status to VERIFIED
 * - If the token exists but is expired:
 *   - Updates the email token status to EXPIRED
 * - If the token does not exist, does nothing (or could throw an exception)
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailTokenRepository emailTokenRepository;
    private final UserRepository userRepository;

    /**
     * Verifies the given token and updates the user status accordingly.
     *
     * @param token the verification token string to validate
     */
    @Transactional
    public void verifyToken(String token) {
        EmailToken emailToken = emailTokenRepository.findByToken(token);

        if (emailToken == null) {
            // Token not found - do nothing
            return;
        }

        if (emailToken.getStatus() == EmailToken.VerificationStatus.EXPIRED) {
            // Token already expired - no further action needed
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();

        if (emailToken.getExpiresAt().isBefore(now) || emailToken.getExpiresAt().isEqual(now)) {
            // Token has expired
            emailToken.setStatus(EmailToken.VerificationStatus.EXPIRED);
            emailTokenRepository.save(emailToken);
            return;
        }

        if (emailToken.getStatus() == EmailToken.VerificationStatus.PENDING) {
            // Token is valid and not expired - update user status
            User user = emailToken.getUser();
            if (user != null) {
                user.setStatus(User.Status.ACTIVE);
                user.setEmailVerifiedAt(now);
                userRepository.save(user);
            }
            emailToken.setStatus(EmailToken.VerificationStatus.VERIFIED);
            emailTokenRepository.save(emailToken);
        }
    }
}