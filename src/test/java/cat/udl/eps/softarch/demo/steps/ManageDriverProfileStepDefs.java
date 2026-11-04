package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ManageDriverProfileStepDefs {
    private final StepDefs stepDefs;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;

    public ManageDriverProfileStepDefs(StepDefs stepDefs, UserRepository userRepository,
        DriverProfileRepository driverProfileRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    private DriverProfile profileOf(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return driverProfileRepository.findAllByUser(user).stream().findFirst().orElseThrow();
    }

    @Given("There is a driver profile for user {string}")
    public void thereIsADriverProfileForUser(String email) {
        DriverProfile profile = new DriverProfile();
        profile.setUser(userRepository.findByEmail(email).orElseThrow());
        profile.setLicenceFrontDoc("front-doc");
        profile.setLicenceBackDoc("back-doc");
        driverProfileRepository.save(profile);
    }

    @When("I create a new driver profile")
    public void iCreateANewDriverProfile() throws Throwable {
        DriverProfile profile = new DriverProfile();
        profile.setLicenceFrontDoc("front-doc");
        profile.setLicenceBackDoc("back-doc");
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/driverProfiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(profile))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I create a new driver profile with verification status {string}")
    public void iCreateANewDriverProfileWithStatus(String status) throws Throwable {
        DriverProfile profile = new DriverProfile();
        profile.setLicenceFrontDoc("front-doc");
        profile.setLicenceBackDoc("back-doc");
        profile.setVerificationStatus(DriverProfile.VerificationStatus.valueOf(status));
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/driverProfiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(profile))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @And("The new driver profile has verification status {string}")
    public void theNewDriverProfileHasStatus(String status) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.verificationStatus", is(status)));
    }

    @When("I retrieve the driver profile for user {string}")
    public void iRetrieveTheDriverProfileForUser(String email) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(profileOf(email).getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I edit the licence front document of the driver profile for user {string}")
    public void iEditTheLicenceFrontDocument(String email) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                patch(profileOf(email).getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"licenceFrontDoc\":\"new-front-doc\"}")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @And("The new driver profile is owned by {string}")
    public void theNewDriverProfileIsOwnedBy(String email) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri + "/user")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.email", is(email)));
    }
}
