package cat.udl.eps.softarch.demo.service;

import cat.udl.eps.softarch.demo.domain.Document;
import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.repository.DocumentRepository;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the upload of driver licence documents (front and back) and their
 * re-upload after a driver profile has been rejected.
 * All methods are transactional.
 */
@Service
@RequiredArgsConstructor
public class DocumentUploadService {

    public static final long MAX_FILE_SIZE = 10_485_760L; // 10 MB
    public static final Set<String> ALLOWED_CONTENT_TYPES =
        Set.of("image/jpeg", "image/png", "application/pdf");

    private static final String ENTITY_TYPE = "DriverProfile";

    private final DocumentRepository documentRepository;
    private final DriverProfileRepository driverProfileRepository;

    @Value("${pickcar.upload-dir:${java.io.tmpdir}/pickcar-uploads}")
    private String uploadDir;

    /** Uploads the front side of the licence of the given driver profile. */
    @Transactional
    public Document uploadLicenceFront(Long driverId, MultipartFile file) {
        DriverProfile profile = getProfile(driverId);
        validateFile(file);
        Document document = store(profile, file, "licence-front");
        profile.setLicenceFrontDoc(String.valueOf(document.getId()));
        driverProfileRepository.save(profile);
        return document;
    }

    /** Uploads the back side of the licence of the given driver profile. */
    @Transactional
    public Document uploadLicenceBack(Long driverId, MultipartFile file) {
        DriverProfile profile = getProfile(driverId);
        validateFile(file);
        Document document = store(profile, file, "licence-back");
        profile.setLicenceBackDoc(String.valueOf(document.getId()));
        driverProfileRepository.save(profile);
        return document;
    }

    /**
     * Validates both licence files: both must be present, of an allowed type
     * and within the size limit.
     *
     * @throws IllegalArgumentException if any of the files is invalid
     */
    public void validateLicenceFiles(MultipartFile front, MultipartFile back) {
        validateFile(front);
        validateFile(back);
    }

    /**
     * Replaces both licence documents of a REJECTED profile and puts it back to
     * PENDING review, clearing the previous rejection reason.
     *
     * @throws IllegalStateException if the profile is not REJECTED
     */
    @Transactional
    public DriverProfile reuploadAfterRejection(Long driverId, MultipartFile front, MultipartFile back) {
        DriverProfile profile = getProfile(driverId);
        if (profile.getVerificationStatus() != DriverProfile.VerificationStatus.REJECTED) {
            throw new IllegalStateException("Documents can only be re-uploaded after a rejection");
        }
        validateLicenceFiles(front, back);

        Document newFront = store(profile, front, "licence-front");
        Document newBack = store(profile, back, "licence-back");
        profile.setLicenceFrontDoc(String.valueOf(newFront.getId()));
        profile.setLicenceBackDoc(String.valueOf(newBack.getId()));
        profile.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
        profile.setRejectionReason(null);
        return driverProfileRepository.save(profile);
    }

    private DriverProfile getProfile(Long driverId) {
        return driverProfileRepository.findOneById(driverId)
            .orElseThrow(() -> new EntityNotFoundException("DriverProfile not found with id: " + driverId));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A licence file is required");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type: " + file.getContentType());
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 10 MB");
        }
    }

    private Document store(DriverProfile profile, MultipartFile file, String kind) {
        String original = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
            ? kind : Path.of(file.getOriginalFilename()).getFileName().toString();
        Path target = Path.of(uploadDir, "driver-" + profile.getId(), UUID.randomUUID() + "-" + original);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file " + original, e);
        }

        Document document = new Document();
        document.setFileName(original);
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStoragePath(target.toString());
        document.setStatus(Document.Status.UPLOADED);
        document.setEntityType(ENTITY_TYPE);
        document.setEntityId(profile.getId());
        document.setUploadedBy(profile.getUser());
        return documentRepository.save(document);
    }
}
