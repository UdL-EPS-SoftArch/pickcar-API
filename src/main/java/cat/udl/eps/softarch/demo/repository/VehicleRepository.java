package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface VehicleRepository extends CrudRepository<Vehicle, Long>, PagingAndSortingRepository<Vehicle, Long> {

    String OWNERSHIP_CLAUSE =
        "v.company.owner.email = ?#{authentication.name} " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT v FROM Vehicle v WHERE " + OWNERSHIP_CLAUSE)
    Page<Vehicle> findAll(Pageable pageable);

    @Override
    @RestResource(exported = false)
    @Query("SELECT v FROM Vehicle v WHERE " + OWNERSHIP_CLAUSE)
    List<Vehicle> findAll(Sort sort);

    @Override
    @RestResource(exported = false)
    @Query("SELECT v FROM Vehicle v WHERE " + OWNERSHIP_CLAUSE)
    List<Vehicle> findAll();

    @Override
    @Query("SELECT v FROM Vehicle v WHERE v.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<Vehicle> findById(@Param("id") Long id);

    @Query("SELECT v FROM Vehicle v WHERE v.company = :company AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Vehicle> findByCompany(@Param("company") Company company, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.status = :status AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Vehicle> findByStatus(@Param("status") Vehicle.Status status, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.pickupPointId = :pickupPointId AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Vehicle> findByPickupPointId(@Param("pickupPointId") Long pickupPointId, Pageable pageable);

    // Internal lookups for services/tests (no authentication context): not exposed over REST
    @RestResource(exported = false)
    Optional<Vehicle> findByPlate(String plate);

    @RestResource(exported = false)
    List<Vehicle> findAllByCompany(Company company);
}
