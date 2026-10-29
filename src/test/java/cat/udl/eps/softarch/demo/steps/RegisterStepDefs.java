package cat.udl.eps.softarch.demo.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RegisterStepDefs {
  private final StepDefs stepDefs;
  private final UserRepository userRepository;

  public RegisterStepDefs(StepDefs stepDefs, UserRepository userRepository) {
    this.stepDefs = stepDefs;
    this.userRepository = userRepository;
  }

  @Given("There is no registered user with email {string}")
  public void thereIsNoRegisteredUserWithEmail(String email) {
    assertFalse(userRepository.findByEmail(email).isPresent(), "User \"" + email + "\" shouldn't exist");
  }

  @Given("There is a registered user with email {string} and password {string}")
  public void thereIsARegisteredUserWithEmailAndPassword(String email, String password) {
    if (userRepository.findByEmail(email).isEmpty()) {
      User user = new User();
      user.setEmail(email);
      user.setPassword(password);
      user.encodePassword();
      userRepository.save(user);
    }
  }

  @And("I can login with email {string} and password {string}")
  public void iCanLoginWithEmailAndPassword(String email, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = email;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity")
            .accept(MediaType.APPLICATION_JSON)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @And("I cannot login with email {string} and password {string}")
  public void iCannotLoginWithEmailAndPassword(String email, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = email;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity")
            .accept(MediaType.APPLICATION_JSON)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  @When("I register a new user with email {string} and password {string}")
  public void iRegisterANewUserWithEmailAndPassword(String email, String password) throws Throwable {
    register(Map.of("email", email, "password", password));
  }

  @When("I register a new user with email {string}, password {string} and role {string}")
  public void iRegisterANewUserWithEmailPasswordAndRole(String email, String password, String role) throws Throwable {
    register(Map.of("email", email, "password", password, "role", role));
  }

  private void register(Map<String, String> body) throws Throwable {
    stepDefs.result = stepDefs.mockMvc.perform(
            post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(body))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
  }

  @And("It has been created a user with email {string}, the password is not returned")
  public void itHasBeenCreatedAUserWithEmail(String email) throws Throwable {
    User user = userRepository.findByEmail(email).orElseThrow();
    stepDefs.result = stepDefs.mockMvc.perform(
            get(user.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.email", is(email)))
            .andExpect(jsonPath("$.password").doesNotExist());
  }

  @And("It has not been created a user with email {string}")
  public void itHasNotBeenCreatedAUserWithEmail(String email) {
    assertFalse(userRepository.findByEmail(email).isPresent(), "User \"" + email + "\" shouldn't exist");
  }

  @Then("The user with email {string} has role {string} and status {string}")
  public void theUserHasRoleAndStatus(String email, String role, String status) {
    User user = userRepository.findByEmail(email).orElseThrow();
    assertEquals(User.Role.valueOf(role), user.getRole());
    assertEquals(User.Status.valueOf(status), user.getStatus());
  }

  @When("I retrieve the user with email {string}")
  public void iRetrieveTheUserWithEmail(String email) throws Throwable {
    User user = userRepository.findByEmail(email).orElseThrow();
    stepDefs.result = stepDefs.mockMvc.perform(
            get(user.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
  }

  @When("I change the user with email {string} to role {string} and status {string}")
  public void iChangeTheUserRoleAndStatus(String email, String role, String status) throws Throwable {
    User user = userRepository.findByEmail(email).orElseThrow();
    stepDefs.result = stepDefs.mockMvc.perform(
            patch(user.getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of("role", role, "status", status)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
  }
}
