package cat.udl.eps.softarch.demo.service;

import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * RegistrationService handles new user registration.
 * <p>
 * The{@link cat.udl.eps.softarch.demo.handler.UserEventHandler} will automatically
 * set the role to DRIVER, status to PENDING_EMAIL, generate a verification token,
 * and encode the password when the user is persisted via the repository.
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;

    /**
     * Registers a new user with the given email and password.
     * <p>
     * Checks for duplicate email, then persists the user. Spring Data REST
     * @RepositoryEventHandler (UserEventHandler) will trigger @HandleBeforeCreate
     * (sets role/status/token, encodes password) and @HandleAfterCreate
     * (creates and saves EmailToken linked to the user).
     *
     * @param email the user's email (must be unique)
     * @param password the user's password (should meet password policy: min 8 chars, not blank, valid email format)
     * @throws IllegalArgumentException if the email is already registered
     */
    @Transactional
    public void register(String email, String password) {
        // Check for duplicate email
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        // Save user - Spring Data REST @RepositoryEventHandler (UserEventHandler) will:
        // - @HandleBeforeCreate: set role=DRIVER, status=PENDING_EMAIL, generate verification token, encode password
        // - @HandleAfterCreate: create and save EmailToken linked to the user
        userRepository.save(user);
    }
}