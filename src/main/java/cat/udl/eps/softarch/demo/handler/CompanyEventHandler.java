package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.Company.Status;
import cat.udl.eps.softarch.demo.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RepositoryEventHandler
public class CompanyEventHandler {

    final Logger logger = LoggerFactory.getLogger(Company.class);

    @PersistenceContext
    private EntityManager entityManager;

    @HandleBeforeCreate
    public void handleCompanyPreCreate(Company company) {
        logger.info("Before creating: {}", company);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User principal) {
            company.setOwner(principal);
        }
        if (company.getOwner() == null) {
            throw new IllegalArgumentException("Company must have an associated owner");
        }
        // Business rule: new companies always start pending verification.
        company.setStatus(Status.PENDING);
        company.setRejectionReason(null);
    }

    @HandleBeforeSave
    public void handleCompanyPreSave(Company company) {
        logger.info("Before saving: {}", company);
        if (company.getOwner() == null) {
            throw new IllegalArgumentException("Company must have an associated owner");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !(authentication.getPrincipal() instanceof String);
        boolean isAdmin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (company.getId() != null) {
            Long persistedOwnerId = persistedOwnerId(company.getId());
            if (persistedOwnerId != null
                    && !Objects.equals(persistedOwnerId, company.getOwner().getId())) {
                throw new AccessDeniedException("The owner of a Company cannot be changed");
            }

            Status persistedStatus = persistedStatus(company.getId());
            Status newStatus = company.getStatus();
            boolean statusChanged = persistedStatus != null && persistedStatus != newStatus;
            boolean reasonChanged = !Objects.equals(persistedRejectionReason(company.getId()),
                    company.getRejectionReason());

            if ((statusChanged || reasonChanged) && authenticated && !isAdmin) {
                throw new AccessDeniedException(
                        "Only an ADMIN can change the status or rejection reason");
            }
            if (statusChanged && !isValidTransition(persistedStatus, newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status transition: " + persistedStatus + " -> " + newStatus);
            }
        }

        if (company.getStatus() == Status.REJECTED
                && (company.getRejectionReason() == null || company.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("A rejected Company must provide a rejection reason");
        }
    }

    private static boolean isValidTransition(Status from, Status to) {
        return switch (from) {
            case PENDING -> to == Status.APPROVED || to == Status.REJECTED;
            case APPROVED -> to == Status.SUSPENDED;
            case SUSPENDED -> to == Status.APPROVED;
            case REJECTED -> false;
        };
    }

    private Object persistedValue(String field, Long id) {
        Query query = entityManager.createQuery(
                "SELECT c." + field + " FROM Company c WHERE c.id = :id");
        query.setParameter("id", id);
        // Do not flush the dirty entity we are about to validate.
        query.setFlushMode(FlushModeType.COMMIT);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Long persistedOwnerId(Long id) {
        Object value = persistedValue("owner.id", id);
        return value instanceof Number n ? n.longValue() : null;
    }

    private Status persistedStatus(Long id) {
        Object value = persistedValue("status", id);
        if (value instanceof Status status) {
            return status;
        }
        return value instanceof String s ? Status.valueOf(s) : null;
    }

    private String persistedRejectionReason(Long id) {
        return persistedValue("rejectionReason", id) instanceof String r ? r : null;
    }
}
