package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class Document extends UriEntity<Long> {

    public enum Status {
        UPLOADED, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;

    @NotNull
    @Positive
    private Long fileSize;

    @NotBlank
    private String storagePath;

    @Enumerated(EnumType.STRING)
    private Status status = Status.UPLOADED;

    @CreatedDate
    @DateTimeFormat
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant uploadedAt;

    // Type of the entity the document belongs to (e.g. "Vehicle") and its id
    @NotBlank
    private String entityType;

    @NotNull
    private Long entityId;

    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    private User uploadedBy;
}
