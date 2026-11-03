package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource
public interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

  String OWNERSHIP_CLAUSE =
      "u.email = ?#{authentication.name} " +
      "OR ?#{hasRole('ADMIN')} = true";

  @Override
  @Query("SELECT u FROM User u WHERE " + OWNERSHIP_CLAUSE)
  Page<User> findAll(Pageable pageable);

  @Override
  @Query("SELECT u FROM User u WHERE u.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
  Optional<User> findById(@Param("id") Long id);

  // Internal lookup (authentication, tests); not exported to avoid leaking registered emails
  @RestResource(exported = false)
  Optional<User> findByEmail(String email);
}
