package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.EmailToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// Internal use only: tokens must never be exposed over REST
@RepositoryRestResource(exported = false)
public interface EmailTokenRepository extends CrudRepository<EmailToken, Long> {

    @Query("SELECT e FROM EmailToken e WHERE e.token = ?1")
    EmailToken findByToken(String token);
}
