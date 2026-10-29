package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface DriverProfileRepository extends CrudRepository<DriverProfile, Long>, PagingAndSortingRepository<DriverProfile, Long> {

  /**
   * Find driver profiles by user ID with row-level authorization.
   * Only returns profiles where the user is the owner or has ADMIN role.
   */
  @Query("SELECT dp FROM DriverProfile dp WHERE dp.user.id = ?#{authentication.name} OR ?#{hasRole('ADMIN')} = true")
  List<DriverProfile> findByUserId(@Param("userId") String userId);

  /**
   * Find driver profiles with a given verification status.
   */
  List<DriverProfile> findByVerificationStatus(DriverProfile.VerificationStatus status);

  /**
   * Find driver profiles by licence front document.
   */
  List<DriverProfile> findByLicenceFrontDocContaining(String licenceDoc);

  /**
   * Find driver profiles by licence back document.
   */
  List<DriverProfile> findByLicenceBackDocContaining(String licenceDoc);
}
