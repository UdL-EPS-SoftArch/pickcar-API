package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.Document;
import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DocumentRepository;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import cat.udl.eps.softarch.demo.service.DocumentUploadService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentUploadStepDefs {
    private final DocumentUploadService service;
    private final UserRepository userRepository;
    private final DriverProfileRepository profileRepository;
    private final DocumentRepository documentRepository;

    private Long profileId;
    private Exception error;

    public DocumentUploadStepDefs(DocumentUploadService service, UserRepository userRepository,
        DriverProfileRepository profileRepository, DocumentRepository documentRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.documentRepository = documentRepository;
    }

    private static MockMultipartFile file(String name, String type) {
        return new MockMultipartFile("file", name, type, "content".getBytes());
    }

    private DriverProfile profile() {
        return profileRepository.findOneById(profileId).orElseThrow();
    }

    @Given("a driver profile exists for licence upload")
    public void aDriverProfileExists() {
        User user = new User();
        user.setEmail("licence-driver@sample.app");
        user.setPassword("password");
        user.encodePassword();
        user = userRepository.save(user);

        DriverProfile dp = new DriverProfile();
        dp.setUser(user);
        dp.setLicenceFrontDoc("pending");
        dp.setLicenceBackDoc("pending");
        profileId = profileRepository.save(dp).getId();
    }

    @Given("the licence profile is rejected with reason {string}")
    public void theProfileIsRejected(String reason) {
        DriverProfile dp = profile();
        dp.setVerificationStatus(DriverProfile.VerificationStatus.REJECTED);
        dp.setRejectionReason(reason);
        profileRepository.save(dp);
    }

    @When("I upload the licence front {string} of type {string}")
    public void iUploadFront(String name, String type) {
        try {
            service.uploadLicenceFront(profileId, file(name, type));
        } catch (Exception e) {
            error = e;
        }
    }

    @When("I upload the licence back {string} of type {string}")
    public void iUploadBack(String name, String type) {
        try {
            service.uploadLicenceBack(profileId, file(name, type));
        } catch (Exception e) {
            error = e;
        }
    }

    @When("I upload the licence front {string} of type {string} and size {int} bytes")
    public void iUploadFrontWithSize(String name, String type, int size) {
        try {
            service.uploadLicenceFront(profileId, new MockMultipartFile("file", name, type, new byte[size]));
        } catch (Exception e) {
            error = e;
        }
    }

    @When("I validate a licence front {string} of type {string} without a back side")
    public void iValidateFrontOnly(String name, String type) {
        try {
            service.validateLicenceFiles(file(name, type), null);
        } catch (Exception e) {
            error = e;
        }
    }

    @When("I validate an empty licence front and a back {string} of type {string}")
    public void iValidateEmptyFront(String name, String type) {
        try {
            service.validateLicenceFiles(new MockMultipartFile("file", name, type, new byte[0]), file(name, type));
        } catch (Exception e) {
            error = e;
        }
    }

    @When("I re-upload the licence front {string} and back {string}")
    public void iReupload(String front, String back) {
        try {
            service.reuploadAfterRejection(profileId, file(front, "image/png"), file(back, "image/png"));
        } catch (Exception e) {
            error = e;
        }
    }

    @Then("a document {string} with status {string} is linked to the profile")
    public void aDocumentIsLinked(String name, String status) {
        assertNull(error);
        Document doc = documentRepository.findByFileName(name).stream().findFirst().orElseThrow();
        assertEquals(Document.Status.valueOf(status), doc.getStatus());
        assertEquals("DriverProfile", doc.getEntityType());
        assertEquals(profileId, doc.getEntityId());
    }

    @And("the profile licence {word} references document {string}")
    public void theProfileReferencesDocument(String side, String name) {
        Document doc = documentRepository.findByFileName(name).stream().findFirst().orElseThrow();
        String reference = switch (side) {
            case "front" -> profile().getLicenceFrontDoc();
            case "back" -> profile().getLicenceBackDoc();
            default -> throw new IllegalArgumentException("Unknown licence side: " + side);
        };
        assertEquals(String.valueOf(doc.getId()), reference);
    }

    @And("no document {string} is stored")
    public void noDocumentIsStored(String name) {
        assertTrue(documentRepository.findByFileName(name).isEmpty());
    }

    @Then("the licence upload fails with {string}")
    public void theUploadFailsWith(String message) {
        assertNotNull(error);
        assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    @Then("the licence profile is {string} with no rejection reason")
    public void theProfileIs(String status) {
        assertNull(error);
        DriverProfile dp = profile();
        assertEquals(DriverProfile.VerificationStatus.valueOf(status), dp.getVerificationStatus());
        assertNull(dp.getRejectionReason());
    }
}
