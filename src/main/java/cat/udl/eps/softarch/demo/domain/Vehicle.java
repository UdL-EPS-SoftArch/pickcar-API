package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class Vehicle extends UriEntity<Long> {

    public enum Status {
        PENDING, ACTIVE, PAUSED, REJECTED, RETIRED
    }

    public enum CancellationPolicy {
        FLEXIBLE, STRICT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String plate;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @Positive
    @Column(name = "manufacture_year")
    private Integer year;

    @NotBlank
    private String category;

    @NotBlank
    private String transmission;

    @NotBlank
    private String fuelType;

    @NotNull
    @Positive
    private Integer seats;

    @NotNull
    @PositiveOrZero
    private Integer dailyPriceCents;

    @NotNull
    @PositiveOrZero
    private Integer depositCents;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CancellationPolicy cancellationPolicy = CancellationPolicy.FLEXIBLE;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // JSON serialized as a string; no Hibernate JSON type dependency in the project
    @Column(length = 10000)
    private String photos;

    @Column(length = 10000)
    private String extras;

    @NotNull
    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    private Company company;

    // Placeholder FK until the PickupPoint aggregate is implemented in another issue
    private Long pickupPointId;

    @CreatedDate
    @DateTimeFormat
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
}
