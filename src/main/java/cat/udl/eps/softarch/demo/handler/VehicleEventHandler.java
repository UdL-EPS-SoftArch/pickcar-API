package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.Vehicle;
import cat.udl.eps.softarch.demo.domain.Vehicle.Status;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
@RepositoryEventHandler
public class VehicleEventHandler {

    final Logger logger = LoggerFactory.getLogger(Vehicle.class);

    @PersistenceContext
    private EntityManager entityManager;

    @HandleBeforeCreate
    public void handleVehiclePreCreate(Vehicle vehicle) {
        logger.info("Before creating: {}", vehicle);
        Company company = vehicle.getCompany();
        if (company == null || company.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vehicle must have an associated company");
        }
        // Spring Data REST resolves associations to stubs carrying only the id, so
        // the owner has to be read from the persisted company.
        Company persistedCompany = persistedCompany(company.getId());
        if (persistedCompany == null || persistedCompany.getOwner() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vehicle must have an associated company with an owner");
        }

        if (!isAdmin()) {
            String email = authenticatedEmail();
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Authentication is required to create a vehicle");
            }
            if (!email.equals(persistedCompany.getOwner().getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A vehicle can only be created for a company owned by the authenticated user");
            }
        }

        // Business rule: new vehicles always start pending verification.
        vehicle.setStatus(Status.PENDING);
    }

    @HandleBeforeSave
    public void handleVehiclePreSave(Vehicle vehicle) {
        logger.info("Before saving: {}", vehicle);
        if (vehicle.getCompany() == null || vehicle.getCompany().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vehicle must have an associated company");
        }

        if (vehicle.getId() != null) {
            Long persistedCompanyId = persistedCompanyId(vehicle.getId());
            if (persistedCompanyId != null
                    && !Objects.equals(persistedCompanyId, vehicle.getCompany().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The company of a Vehicle cannot be changed");
            }

            Status persistedStatus = persistedStatus(vehicle.getId());
            Status newStatus = vehicle.getStatus();
            boolean statusChanged = persistedStatus != null && persistedStatus != newStatus;

            if (statusChanged && !isAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only an ADMIN can change the status of a vehicle");
            }
            if (statusChanged && !isValidTransition(persistedStatus, newStatus)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status transition: " + persistedStatus + " -> " + newStatus);
            }
        }

        // A vehicle can only be available when the company itself has been approved.
        if (vehicle.getStatus() == Status.ACTIVE
                && persistedCompanyStatus(vehicle.getCompany().getId()) != Company.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A vehicle can only be ACTIVE when its company is APPROVED");
        }
    }

    private static boolean isValidTransition(Status from, Status to) {
        return switch (from) {
            case PENDING -> to == Status.ACTIVE || to == Status.REJECTED;
            case ACTIVE -> to == Status.PAUSED || to == Status.RETIRED;
            case PAUSED -> to == Status.ACTIVE || to == Status.RETIRED;
            case REJECTED, RETIRED -> false;
        };
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isAuthenticated() {
        Authentication authentication = authentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !(authentication.getPrincipal() instanceof String);
    }

    private boolean isAdmin() {
        return isAuthenticated() && authentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String authenticatedEmail() {
        // Works for both the domain User and Spring Security's UserDetails principal.
        return isAuthenticated() ? authentication().getName() : null;
    }

    private Company persistedCompany(Long companyId) {
        Query query = entityManager.createQuery(
                "SELECT c FROM Company c WHERE c.id = :id");
        query.setParameter("id", companyId);
        query.setFlushMode(FlushModeType.COMMIT);
        try {
            return (Company) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Object persistedValue(String field, Long id) {
        Query query = entityManager.createQuery(
                "SELECT v." + field + " FROM Vehicle v WHERE v.id = :id");
        query.setParameter("id", id);
        // Do not flush the dirty entity we are about to validate.
        query.setFlushMode(FlushModeType.COMMIT);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Long persistedCompanyId(Long id) {
        Object value = persistedValue("company.id", id);
        return value instanceof Number n ? n.longValue() : null;
    }

    private Status persistedStatus(Long id) {
        Object value = persistedValue("status", id);
        if (value instanceof Status status) {
            return status;
        }
        return value instanceof String s ? Status.valueOf(s) : null;
    }

    private Company.Status persistedCompanyStatus(Long companyId) {
        if (companyId == null) {
            return null;
        }
        Query query = entityManager.createQuery(
                "SELECT c.status FROM Company c WHERE c.id = :id");
        query.setParameter("id", companyId);
        query.setFlushMode(FlushModeType.COMMIT);
        try {
            Object value = query.getSingleResult();
            if (value instanceof Company.Status status) {
                return status;
            }
            return value instanceof String s ? Company.Status.valueOf(s) : null;
        } catch (NoResultException e) {
            return null;
        }
    }
}
