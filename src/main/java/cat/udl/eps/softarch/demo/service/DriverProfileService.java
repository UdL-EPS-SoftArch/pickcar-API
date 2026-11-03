package cat.udl.eps.softarch.demo.service;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for DriverProfile business operations.
 * All methods are transactional and enforce business rules.
 */
@Service
@RequiredArgsConstructor
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;

    /**
     * Creates a new driver profile linked to the specified user.
     * The profile starts with PENDING verification status.
     */
    @Transactional
    public DriverProfile createProfile(DriverProfile profile) {
        // A driver profile must be associated with a user; the
        // @HandleBeforeCreate handler enforces this and sets PENDING status.
        if (profile.getUser() == null) {
            throw new IllegalArgumentException("DriverProfile must have an associated user");
        }
        // Status is set to PENDING by the @HandleBeforeCreate handler
        return driverProfileRepository.save(profile);
    }

    /**
     * Retrieves a driver profile by its ID.
     */
    @Transactional(readOnly = true)
    public DriverProfile getProfileById(Long profileId) {
        return driverProfileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("DriverProfile not found with id: " + profileId));
    }

    /**
     * Retrieves all driver profiles for a given user.
     */
    @Transactional(readOnly = true)
    public List<DriverProfile> getProfilesByUserId(Long userId) {
        // The repository method uses SpEL authentication.name which expects a String.
        return driverProfileRepository.findByUserId(userId.toString());
    }

    /**
     * Approves a driver profile (changes status from PENDING to APPROVED).
     * Only profiles in PENDING state can be approved.
     */
    @Transactional
    public DriverProfile approveProfile(Long profileId) {
        DriverProfile profile = getProfileById(profileId);
        if (profile.getVerificationStatus() != DriverProfile.VerificationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING profiles can be approved");
        }
        profile.setVerificationStatus(DriverProfile.VerificationStatus.APPROVED);
        return driverProfileRepository.save(profile);
    }

    /**
     * Rejects a driver profile (changes status from PENDING to REJECTED).
     * A rejection reason is required.
     */
    @Transactional
    public DriverProfile rejectProfile(Long profileId, String rejectionReason) {
        DriverProfile profile = getProfileById(profileId);
        if (profile.getVerificationStatus() != DriverProfile.VerificationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING profiles can be rejected");
        }
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        profile.setVerificationStatus(DriverProfile.VerificationStatus.REJECTED);
        profile.setRejectionReason(rejectionReason);
        return driverProfileRepository.save(profile);
    }

    /**
     * Updates the licence documents for a driver profile.
     */
    @Transactional
    public DriverProfile updateLicenceDocs(Long profileId, String licenceFrontDoc, String licenceBackDoc) {
        DriverProfile profile = getProfileById(profileId);
        profile.setLicenceFrontDoc(licenceFrontDoc);
        profile.setLicenceBackDoc(licenceBackDoc);
        return driverProfileRepository.save(profile);
    }

    /**
     * Deletes a driver profile by ID.
     */
    @Transactional
    public void deleteProfile(Long profileId) {
        DriverProfile profile = getProfileById(profileId);
        driverProfileRepository.delete(profile);
    }
}