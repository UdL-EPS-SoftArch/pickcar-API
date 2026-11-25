package cat.udl.eps.softarch.demo.steps;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.Vehicle;
import cat.udl.eps.softarch.demo.repository.CompanyRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import cat.udl.eps.softarch.demo.repository.VehicleRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class ManageVehicleStepDefs {
    private final StepDefs stepDefs;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final VehicleRepository vehicleRepository;

    public ManageVehicleStepDefs(StepDefs stepDefs, UserRepository userRepository,
                                 CompanyRepository companyRepository, VehicleRepository vehicleRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.vehicleRepository = vehicleRepository;
    }

    private Company companyWithCif(String cif) {
        return companyRepository.findByCif(cif).orElseThrow();
    }

    private Vehicle vehicleWithPlate(String plate) {
        return vehicleRepository.findByPlate(plate).orElseThrow();
    }

    private Company newCompany(String cif, String ownerEmail, String status) {
        Company company = new Company();
        company.setLegalName("Sample Rentals SL");
        company.setCif(cif);
        company.setAddress("Carrer Major 1, Lleida");
        company.setCifDocument("cif-doc");
        company.setInsuranceDocument("insurance-doc");
        company.setOwner(userRepository.findByEmail(ownerEmail).orElseThrow());
        company.setStatus(Company.Status.valueOf(status));
        return company;
    }

    private Vehicle newVehicle(String plate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(plate);
        vehicle.setBrand("Sample");
        vehicle.setModel("Model S");
        vehicle.setYear(2024);
        vehicle.setCategory("SEDAN");
        vehicle.setTransmission("AUTOMATIC");
        vehicle.setFuelType("ELECTRIC");
        vehicle.setSeats(5);
        vehicle.setDailyPriceCents(8000);
        vehicle.setDepositCents(20000);
        vehicle.setCancellationPolicy(Vehicle.CancellationPolicy.FLEXIBLE);
        return vehicle;
    }

    @Given("There is a company with CIF {string} owned by {string} with status {string}")
    public void thereIsACompany(String cif, String email, String status) {
        companyRepository.save(newCompany(cif, email, status));
    }

    @Given("There is a vehicle with plate {string} for company {string}")
    public void thereIsAVehicle(String plate, String cif) {
        Vehicle vehicle = newVehicle(plate);
        vehicle.setCompany(companyWithCif(cif));
        vehicleRepository.save(vehicle);
    }

    @Given("There is a vehicle with plate {string} for company {string} with status {string}")
    public void thereIsAVehicleWithStatus(String plate, String cif, String status) {
        Vehicle vehicle = newVehicle(plate);
        vehicle.setCompany(companyWithCif(cif));
        vehicle.setStatus(Vehicle.Status.valueOf(status));
        vehicleRepository.save(vehicle);
    }

    @When("I create a vehicle with plate {string} for company {string} with status {string}")
    public void iCreateAVehicle(String plate, String cif, String status) throws Throwable {
        Vehicle vehicle = newVehicle(plate);
        vehicle.setCompany(companyWithCif(cif));
        vehicle.setStatus(Vehicle.Status.valueOf(status));
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stepDefs.mapper.writeValueAsString(vehicle))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @When("I retrieve the vehicle with plate {string}")
    public void iRetrieveTheVehicle(String plate) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(vehicleWithPlate(plate).getUri())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @When("I change the status of the vehicle with plate {string} to {string}")
    public void iChangeTheStatus(String plate, String status) throws Throwable {
        patchVehicle(plate, "{\"status\":\"" + status + "\"}");
    }

    @When("I change the company of the vehicle with plate {string} to {string}")
    public void iChangeTheCompany(String plate, String cif) throws Throwable {
        patchVehicle(plate, "{\"company\":\"" + companyWithCif(cif).getUri() + "\"}");
    }

    private void patchVehicle(String plate, String content) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                patch(vehicleWithPlate(plate).getUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @And("The vehicle with plate {string} has status {string}")
    public void theVehicleHasStatus(String plate, String status) {
        assertEquals(Vehicle.Status.valueOf(status), vehicleWithPlate(plate).getStatus());
    }

    @And("The vehicle with plate {string} belongs to company {string}")
    public void theVehicleBelongsToCompany(String plate, String cif) {
        assertEquals(companyWithCif(cif).getId(), vehicleWithPlate(plate).getCompany().getId());
    }
}
