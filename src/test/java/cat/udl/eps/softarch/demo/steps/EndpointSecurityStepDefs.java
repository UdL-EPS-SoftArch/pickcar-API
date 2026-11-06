package cat.udl.eps.softarch.demo.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class EndpointSecurityStepDefs {

    private final StepDefs stepDefs;

    public EndpointSecurityStepDefs(StepDefs stepDefs) {
        this.stepDefs = stepDefs;
    }

    @When("I send a {word} request to {string}")
    public void iSendARequestTo(String method, String path) throws Exception {
        stepDefs.result = stepDefs.mockMvc.perform(
                request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()));
    }
}
