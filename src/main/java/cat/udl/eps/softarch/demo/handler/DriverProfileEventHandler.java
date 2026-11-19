package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.DriverProfile.VerificationStatus;
import cat.udl.eps.softarch.demo.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RepositoryEventHandler
public class DriverProfileEventHandler {

    final Logger logger = LoggerFactory.getLogger(DriverProfile.class);

    @PersistenceContext
    private EntityManager entityManager;

    @HandleBeforeCreate
    public void handleDriverProfilePreCreate(DriverProfile driverProfile) {
        logger.info("Before creating: {}", driverProfile);
        // Business rule: a driver profile is owned by the authenticated user.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User principal) {
            driverProfile.setUser(principal);
        }
        if (driverProfile.getUser() == null) {
            throw new IllegalArgumentException("DriverProfile must have an associated user");
        }
        // Business rule: new profiles start with PENDING verification.
        driverProfile.setVerificationStatus(VerificationStatus.PENDING);
    }

    @HandleBeforeSave
    public void handleDriverProfilePreSave(DriverProfile driverProfile) {
        logger.info("Before saving: {}", driverProfile);
        if (driverProfile.getUser() == null) {
            throw new IllegalArgumentException("DriverProfile must have an associated user");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !(authentication.getPrincipal() instanceof String);
        boolean isAdmin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (driverProfile.getId() != null) {
            // Read the persisted values without flushing the pending changes, so we can
            // tell whether a protected field was actually modified by this request.
            Long persistedUserId = persistedUserId(driverProfile.getId());
            if (persistedUserId != null
                    && !Objects.equals(persistedUserId, driverProfile.getUser().getId())) {
                throw new AccessDeniedException("The user of a DriverProfile cannot be changed");
            }

            VerificationStatus persistedStatus = persistedStatus(driverProfile.getId());
            VerificationStatus newStatus = driverProfile.getVerificationStatus();
            String persistedReason = persistedRejectionReason(driverProfile.getId());
            String newReason = driverProfile.getRejectionReason();
            boolean statusChanged = persistedStatus != null && persistedStatus != newStatus;
            boolean reasonChanged = !Objects.equals(persistedReason, newReason);

            if ((statusChanged || reasonChanged) && authenticated && !isAdmin) {
                throw new AccessDeniedException(
                        "Only an ADMIN can change the verification status or rejection reason");
            }
            if (statusChanged && isAdmin) {
                if (persistedStatus != VerificationStatus.PENDING
                        || (newStatus != VerificationStatus.APPROVED
                        && newStatus != VerificationStatus.REJECTED)) {
                    throw new IllegalArgumentException("Only PENDING profiles can be approved or rejected");
                }
            }
        }

        // Business rule: a REJECTED profile must provide a rejection reason.
        if (driverProfile.getVerificationStatus() == VerificationStatus.REJECTED
                && driverProfile.getRejectionReason() == null) {
            throw new IllegalArgumentException("A rejected DriverProfile must provide a rejection reason");
        }
    }

    private Object persistedValue(String field, Long id) {
        Query query = entityManager.createQuery(
                "SELECT d." + field + " FROM DriverProfile d WHERE d.id = :id");
        query.setParameter("id", id);
        // Do not flush the dirty entity we are about to validate.
        query.setFlushMode(FlushModeType.COMMIT);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Long persistedUserId(Long id) {
        Object value = persistedValue("user.id", id);
        if (value instanceof Long userId) {
            return userId;
        }
        return value instanceof Integer userId ? userId.longValue() : null;
    }

    private VerificationStatus persistedStatus(Long id) {
        Object value = persistedValue("verificationStatus", id);
        if (value instanceof VerificationStatus status) {
            return status;
        }
        return value instanceof String status ? VerificationStatus.valueOf(status) : null;
    }

    private String persistedRejectionReason(Long id) {
        Object value = persistedValue("rejectionReason", id);
        return value instanceof String reason ? reason : null;
    }

    @HandleBeforeDelete
    public void handleDriverProfilePreDelete(DriverProfile driverProfile) {
        logger.info("Before deleting: {}", driverProfile);
    }
}
