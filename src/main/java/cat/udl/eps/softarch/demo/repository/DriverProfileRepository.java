package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    String OWNERSHIP_CLAUSE =
        "e.user.email = ?#{authentication.name} " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT e FROM DriverProfile e WHERE " + OWNERSHIP_CLAUSE)
    Page<DriverProfile> findAll(Pageable pageable);

    @Override
    @Query("SELECT e FROM DriverProfile e WHERE e.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<DriverProfile> findById(@Param("id") Long id);

    @Query("SELECT e FROM DriverProfile e WHERE e.user = :user AND (" + OWNERSHIP_CLAUSE + ")")
    Page<DriverProfile> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT e FROM DriverProfile e WHERE e.verificationStatus = :status AND (" + OWNERSHIP_CLAUSE + ")")
    Page<DriverProfile> findByVerificationStatus(@Param("status") DriverProfile.VerificationStatus status, Pageable pageable);

    // Internal lookups for services/tests (no authentication context): not exposed over REST
    @RestResource(exported = false)
    Optional<DriverProfile> findOneById(Long id);

    @RestResource(exported = false)
    List<DriverProfile> findAllByUser(User user);
}
