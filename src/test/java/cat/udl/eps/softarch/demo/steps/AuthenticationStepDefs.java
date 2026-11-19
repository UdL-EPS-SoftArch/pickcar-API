package cat.udl.eps.softarch.demo.steps;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import cat.udl.eps.softarch.demo.domain.User;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class AuthenticationStepDefs {

    public static String currentUsername;
    public static String currentPassword;

    @Before
    public void setup() {
        // Clear authentication credentials at the start of every test.
        AuthenticationStepDefs.currentPassword = "";
        AuthenticationStepDefs.currentUsername = "";
        SecurityContextHolder.clearContext();
    }

    static RequestPostProcessor authenticate() {
        return currentUsername!=null ? httpBasic(currentUsername, currentPassword) : anonymous();
    }

    @Given("I am authenticated as an ADMIN")
    public void iAmAuthenticatedAsAnAdmin() {
        authenticateAs(User.Role.ADMIN);
    }

    @Given("I am authenticated as a DRIVER")
    public void iAmAuthenticatedAsADriver() {
        authenticateAs(User.Role.DRIVER);
    }

    private void authenticateAs(User.Role role) {
        // Use a UserDetails principal (not a domain User) so the DriverProfile
        // @HandleBeforeCreate handler does not replace the profile's user with it.
        var principal = org.springframework.security.core.userdetails.User
                .withUsername(role == User.Role.ADMIN ? "admin@sample.app" : "user@sample.app")
                .password("password")
                .roles(role.name())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Given("^I login as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void iLoginAsWithPassword(String username, String password) {
        AuthenticationStepDefs.currentUsername = username;
        AuthenticationStepDefs.currentPassword = password;
    }

    @Given("^I'm not logged in$")
    public void iMNotLoggedIn() {
        currentUsername = currentPassword = null;
    }
}
