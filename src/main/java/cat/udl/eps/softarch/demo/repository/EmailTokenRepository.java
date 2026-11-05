package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.EmailToken;
import org.springframework.data.repository.CrudRepository;

public interface EmailTokenRepository extends CrudRepository<EmailToken, Long> {
}