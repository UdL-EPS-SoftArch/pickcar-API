package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class DriverProfileEventHandler {

    final Logger logger = LoggerFactory.getLogger(DriverProfile.class);

    @HandleBeforeCreate
    public void handleDriverProfilePreCreate(DriverProfile driverProfile) {
        logger.info("Before creating: {}", driverProfile);
        // Business rule: a driver profile must be linked to a user.
        if (driverProfile.getUser() == null) {
            throw new IllegalArgumentException("DriverProfile must have an associated user");
        }
        // Business rule: new profiles start with PENDING verification.
        driverProfile.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
    }

    @HandleBeforeSave
    public void handleDriverProfilePreSave(DriverProfile driverProfile) {
        logger.info("Before saving: {}", driverProfile);
        if (driverProfile.getUser() == null) {
            throw new IllegalArgumentException("DriverProfile must have an associated user");
        }
        // Business rule: an APPROVED/REJECTED profile must provide a rejection reason if rejected.
        if (driverProfile.getVerificationStatus() == DriverProfile.VerificationStatus.REJECTED
                && driverProfile.getRejectionReason() == null) {
            throw new IllegalArgumentException("A rejected DriverProfile must provide a rejection reason");
        }
    }

    @HandleBeforeDelete
    public void handleDriverProfilePreDelete(DriverProfile driverProfile) {
        logger.info("Before deleting: {}", driverProfile);
    }
}
