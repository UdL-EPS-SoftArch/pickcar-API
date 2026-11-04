package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class EmailToken {

    public enum TokenType {
        EMAIL_VERIFICATION
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String token;

    @NotBlank
    private String hash;

    private ZonedDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @ManyToOne
    private User user;
}
