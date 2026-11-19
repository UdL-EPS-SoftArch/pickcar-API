package cat.udl.eps.softarch.demo.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import cat.udl.eps.softarch.demo.domain.Record;
import cat.udl.eps.softarch.demo.repository.RecordRepository;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class EndpointSecurityStepDefs {

    private final StepDefs stepDefs;
    private final RecordRepository recordRepository;

    public EndpointSecurityStepDefs(StepDefs stepDefs, RecordRepository recordRepository) {
        this.stepDefs = stepDefs;
        this.recordRepository = recordRepository;
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

    @When("I send a {word} request to change the owner of the record with name {string}")
    public void iSendARequestToChangeTheOwnerOfRecord(String method, String name) throws Exception {
        Record record = recordRepository.findByName(name).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                request(HttpMethod.valueOf(method), record.getUri() + "/ownedBy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownedBy\":\"/users/1\"}")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()));
    }
}
