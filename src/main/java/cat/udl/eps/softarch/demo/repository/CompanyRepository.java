package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Company;
import cat.udl.eps.softarch.demo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface CompanyRepository extends CrudRepository<Company, Long>, PagingAndSortingRepository<Company, Long> {

    String OWNERSHIP_CLAUSE =
        "e.owner.email = ?#{authentication.name} " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT e FROM Company e WHERE " + OWNERSHIP_CLAUSE)
    Page<Company> findAll(Pageable pageable);

    @Override
    @Query("SELECT e FROM Company e WHERE e.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<Company> findById(@Param("id") Long id);

    @Query("SELECT e FROM Company e WHERE e.owner = :owner AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Company> findByOwner(@Param("owner") User owner, Pageable pageable);

    @Query("SELECT e FROM Company e WHERE e.status = :status AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Company> findByStatus(@Param("status") Company.Status status, Pageable pageable);

    // Internal lookups for services/tests (no authentication context): not exposed over REST
    @RestResource(exported = false)
    List<Company> findAllByOwner(User owner);

    @RestResource(exported = false)
    Optional<Company> findByCif(String cif);
}
