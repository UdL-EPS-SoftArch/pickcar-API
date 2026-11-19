package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.CompanyRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ManageCompanyStepDefs {
    private final StepDefs stepDefs;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public ManageCompanyStepDefs(StepDefs stepDefs, UserRepository userRepository,
        CompanyRepository companyRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    private Company companyWithCif(String cif) {
        return companyRepository.findByCif(cif).orElseThrow();
    }

    private Company newCompany(String cif) {
        Company company = new Company();
        company.setLegalName("Sample Rentals SL");
        company.setCif(cif);
        company.setAddress("Carrer Major 1, Lleida");
        company.setCifDocument("cif-doc");
        company.setInsuranceDocument("insurance-doc");
        return company;
    }

    private void postCompany(Company company) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(company))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Given("There is a company with CIF {string} owned by {string}")
    public void thereIsACompany(String cif, String email) {
        Company company = newCompany(cif);
        company.setOwner(userRepository.findByEmail(email).orElseThrow());
        companyRepository.save(company);
    }

    @When("I create a new company with CIF {string} and status {string}")
    public void iCreateACompanyWithStatus(String cif, String status) throws Throwable {
        Company company = newCompany(cif);
        company.setStatus(Company.Status.valueOf(status));
        postCompany(company);
    }

    @When("I create a new company with CIF {string} and no documents")
    public void iCreateACompanyWithoutDocuments(String cif) throws Throwable {
        Company company = newCompany(cif);
        company.setCifDocument("");
        company.setInsuranceDocument("");
        postCompany(company);
    }

    @And("The new company has status {string}")
    public void theNewCompanyHasStatus(String status) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.status", is(status)));
    }

    @And("The new company is owned by {string}")
    public void theNewCompanyIsOwnedBy(String email) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri + "/owner")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.email", is(email)));
    }

    @When("I retrieve the company with CIF {string}")
    public void iRetrieveTheCompany(String cif) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(companyWithCif(cif).getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I try to change the status of the company with CIF {string} to {string}")
    public void iTryToChangeStatus(String cif, String status) throws Throwable {
        patchCompany(cif, "{\"status\":\"" + status + "\"}");
    }

    @When("I try to reassign the company with CIF {string} to {string}")
    public void iTryToReassign(String cif, String newOwnerEmail) throws Throwable {
        User newOwner = userRepository.findByEmail(newOwnerEmail).orElseThrow();
        patchCompany(cif, "{\"owner\":\"" + newOwner.getUri() + "\"}");
    }

    private void patchCompany(String cif, String content) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                patch(companyWithCif(cif).getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @And("The company with CIF {string} has status {string}")
    public void theCompanyHasStatus(String cif, String status) {
        assertEquals(status, companyWithCif(cif).getStatus().name());
    }

    @And("The company with CIF {string} is owned by {string}")
    public void theCompanyIsOwnedBy(String cif, String email) {
        assertEquals(email, companyWithCif(cif).getOwner().getEmail());
    }
}
