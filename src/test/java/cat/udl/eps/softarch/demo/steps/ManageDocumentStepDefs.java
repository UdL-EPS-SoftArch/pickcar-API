package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.Document;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DocumentRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ManageDocumentStepDefs {
    private final StepDefs stepDefs;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public ManageDocumentStepDefs(StepDefs stepDefs, UserRepository userRepository,
        DocumentRepository documentRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    private Document newDocument(String fileName, long size) {
        Document document = new Document();
        document.setFileName(fileName);
        document.setContentType("application/pdf");
        document.setFileSize(size);
        document.setStoragePath("/uploads/" + fileName);
        document.setEntityType("User");
        document.setEntityId(1L);
        return document;
    }

    @Given("There is a document with file name {string} uploaded by {string}")
    public void thereIsADocumentUploadedBy(String fileName, String username) {
        Document document = newDocument(fileName, 1024);
        document.setUploadedBy(userRepository.findById(username).orElseThrow());
        documentRepository.save(document);
    }

    @When("I upload a document with file name {string} and size {long}")
    public void iUploadADocument(String fileName, long size) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(newDocument(fileName, size)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I upload a document with file name {string} and size {long} and status {string}")
    public void iUploadADocumentWithStatus(String fileName, long size, String status) throws Throwable {
        Document document = newDocument(fileName, size);
        document.setStatus(Document.Status.valueOf(status));
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(document))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @And("The new document has status {string}")
    public void theNewDocumentHasStatus(String status) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.status", is(status)));
    }

    @And("The new document is uploaded by {string}")
    public void theNewDocumentIsUploadedBy(String username) throws Throwable {
        String uri = stepDefs.result.andReturn().getResponse().getHeader("Location");
        stepDefs.mockMvc.perform(
                get(uri + "/uploadedBy")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(jsonPath("$.username", is(username)));
    }

    @When("I retrieve the document with file name {string}")
    public void iRetrieveTheDocument(String fileName) throws Throwable {
        Document document = documentRepository.findByFileName(fileName).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/documents/" + document.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I change the status of the document with file name {string} to {string}")
    public void iChangeTheStatusOfTheDocument(String fileName, String status) throws Throwable {
        Document document = documentRepository.findByFileName(fileName).stream().findFirst().orElseThrow();
        stepDefs.result = stepDefs.mockMvc.perform(
                patch("/documents/" + document.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of("status", status)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }
}
