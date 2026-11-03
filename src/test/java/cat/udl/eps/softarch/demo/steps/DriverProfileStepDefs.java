package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DriverProfileStepDefs {

    private final StepDefs stepDefs;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;

    public DriverProfileStepDefs(StepDefs stepDefs, UserRepository userRepository,
                                 DriverProfileRepository driverProfileRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    @Given("There is a driver profile for user {string}")
    public void thereIsADriverProfileForUser(String username) {
        User user = userRepository.findById(username).orElseThrow();
        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        profile.setVerificationStatus(DriverProfile.VerificationStatus.PENDING);
        driverProfileRepository.save(profile);
    }

    @When("I create a new driver profile for user {string}")
    public void iCreateANewDriverProfileForUser(String username) throws Throwable {
        User user = userRepository.findById(username).orElseThrow();
        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/driverProfiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(profile))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I retrieve the driver profile for user {string}")
    public void iRetrieveTheDriverProfileForUser(String username) throws Throwable {
        User user = userRepository.findById(username).orElseThrow();
        DriverProfile profile = driverProfileRepository.findByUser(user).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                get(profile.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I edit the driver profile for user {string} to have verification status {string}")
    public void iEditTheDriverProfileForUserToHaveVerificationStatus(String username, String status) throws Throwable {
        User user = userRepository.findById(username).orElseThrow();
        DriverProfile profile = driverProfileRepository.findByUser(user).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                patch(profile.getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"verificationStatus\":\"" + status + "\"}")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I edit the driver profile for user {string} to have verification status APPROVED")
    public void iEditTheDriverProfileForUserToHaveVerificationStatusApproved(String username) throws Throwable {
        iEditTheDriverProfileForUserToHaveVerificationStatus(username, "APPROVED");
    }

    @Then("The new driver profile is owned by {string}")
    public void theNewDriverProfileIsOwnedBy(String username) throws Throwable {
        String newProfileUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.result = stepDefs.mockMvc.perform(
                get(newProfileUri + "/user")
                    .accept(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.username", org.hamcrest.Matchers.is(username)));
    }

    @Then("The driver profile for user {string} has verification status {string}")
    public void theDriverProfileForUserHasVerificationStatus(String username, String status) throws Throwable {
        User user = userRepository.findById(username).orElseThrow();
        DriverProfile profile = driverProfileRepository.findByUser(user).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                get(profile.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.verificationStatus", org.hamcrest.Matchers.is(status)));
    }

    @Then("The driver profile for user {string} has verification status APPROVED")
    public void theDriverProfileForUserHasVerificationStatusApproved(String username) throws Throwable {
        theDriverProfileForUserHasVerificationStatus(username, "APPROVED");
    }
}
