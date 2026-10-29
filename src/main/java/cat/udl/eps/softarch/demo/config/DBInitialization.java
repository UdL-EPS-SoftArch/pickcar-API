package cat.udl.eps.softarch.demo.config;
import cat.udl.eps.softarch.demo.domain.Record;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.RecordRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;

@Configuration
public class DBInitialization {
    @Value("${default-password}")
    String defaultPassword;

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final Environment environment;

    public DBInitialization(UserRepository userRepository, RecordRepository recordRepository,
                            Environment environment) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.environment = environment;
    }

    @PostConstruct
    public void initializeDatabase() {
        // Default user
        if (userRepository.findByEmail("demo@sample.app").isEmpty()) {
            User user = new User();
            user.setEmail("demo@sample.app");
            user.setStatus(User.Status.ACTIVE);
            user.setPassword(defaultPassword);
            user.encodePassword();
            userRepository.save(user);
        }
        if (environment.matchesProfiles("test")) {
            // Testing instances
            if (userRepository.findByEmail("admin@sample.app").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@sample.app");
                admin.setRole(User.Role.ADMIN);
                admin.setStatus(User.Status.ACTIVE);
                admin.setPassword(defaultPassword);
                admin.encodePassword();
                userRepository.save(admin);
            }
            if (userRepository.findByEmail("test@sample.app").isEmpty()) {
                User user = new User();
                user.setEmail("test@sample.app");
                user.setPassword(defaultPassword);
                user.encodePassword();
                user = userRepository.save(user);
                cat.udl.eps.softarch.demo.domain.Record record = new Record();
                record.setName("My test record");
                record.setDescription("A record used for testing purposes, nothing more, nothing less...");
                record.setOwnedBy(user);
                recordRepository.save(record);
            }
        }
    }
}
