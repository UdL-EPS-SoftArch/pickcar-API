package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.service.DriverProfileService;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import cat.udl.eps.softarch.demo.domain.User;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DriverProfileStepDefs {

    @Autowired
    private DriverProfileService driverProfileService;

    @Autowired
    private UserRepository userRepository;

    private DriverProfile profile;

    @Given("the application is running")
    public void the_application_is_running() {
        // Application context is initialized by CucumberContextConfiguration
    }

    @Given("a DriverProfile exists with status PENDING")
    public void a_profile_exists_with_status_pending() {
        User user = new User();
        user.setId("user1");
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPassword("password");
        userRepository.save(user);

        DriverProfile dp = new DriverProfile();
        dp.setUser(user);
        dp.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
        dp.setLicenceFrontDoc("front-doc-id");
        dp.setLicenceBackDoc("back-doc-id");
        profile = driverProfileService.createProfile(dp);
        assertEquals(DriverProfile.VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @When("I create a DriverProfile with user {string} and verificationStatus {string}")
    public void i_create_a_driver_profile(String userId, String status) {
        User user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPassword("password");
        userRepository.save(user);

        DriverProfile dp = new DriverProfile();
        dp.setUser(user);
        dp.setLicenceFrontDoc("front-doc");
        dp.setLicenceBackDoc("back-doc");
        dp.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
        profile = driverProfileService.createProfile(dp);
        assertEquals(DriverProfile.VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @Then("the DriverProfile is created with status PENDING")
    public void the_profile_is_created_with_status_pending() {
        assertNotNull(profile);
        assertEquals(DriverProfile.VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @When("I approve the DriverProfile")
    public void i_approve_the_profile() {
        if (profile == null) {
            profile = new DriverProfile();
            profile.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
            profile.setLicenceFrontDoc("front-doc");
            profile.setLicenceBackDoc("back-doc");
            profile = driverProfileService.createProfile(profile);
        }
        profile = driverProfileService.approveProfile(profile.getId());
    }

    @Then("the DriverProfile has status APPROVED")
    public void profile_has_status_approved() {
        assertNotNull(profile);
        assertEquals(DriverProfile.VerificationStatus.APPROVED, profile.getVerificationStatus());
    }

    @When("I reject the DriverProfile with reason {string}")
    public void i_reject_the_profile_with_reason(String reason) {
        if (profile == null) {
            profile = new DriverProfile();
            profile.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
            profile.setLicenceFrontDoc("front-doc");
            profile.setLicenceBackDoc("back-doc");
            profile = driverProfileService.createProfile(profile);
        }
        profile = driverProfileService.rejectProfile(profile.getId(), reason);
    }

    @Then("the DriverProfile has status REJECTED")
    public void profile_has_status_rejected() {
        assertNotNull(profile);
        assertEquals(DriverProfile.VerificationStatus.REJECTED, profile.getVerificationStatus());
    }

    @And("the DriverProfile has rejectionReason {string}")
    public void profile_has_rejection_reason(String reason) {
        assertNotNull(profile);
        assertEquals(reason, profile.getRejectionReason());
    }
}
