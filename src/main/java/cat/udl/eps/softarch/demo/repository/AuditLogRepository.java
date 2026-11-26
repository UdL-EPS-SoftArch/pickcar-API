package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.AuditLog;
import cat.udl.eps.softarch.demo.domain.User;

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
public interface AuditLogRepository extends CrudRepository<AuditLog, Long>, PagingAndSortingRepository<AuditLog, Long> {

    String ADMIN_CLAUSE = "?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT a FROM AuditLog a WHERE " + ADMIN_CLAUSE)
    Page<AuditLog> findAll(Pageable pageable);

    @Override
    @RestResource(exported = false)
    @Query("SELECT a FROM AuditLog a WHERE " + ADMIN_CLAUSE)
    List<AuditLog> findAll(Sort sort);

    @Override
    @RestResource(exported = false)
    @Query("SELECT a FROM AuditLog a WHERE " + ADMIN_CLAUSE)
    List<AuditLog> findAll();

    @Override
    @Query("SELECT a FROM AuditLog a WHERE a.id = :id AND (" + ADMIN_CLAUSE + ")")
    Optional<AuditLog> findById(@Param("id") Long id);

    @Query("SELECT a FROM AuditLog a WHERE a.admin = :admin AND (" + ADMIN_CLAUSE + ")")
    Page<AuditLog> findByAdmin(@Param("admin") User admin, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND (" + ADMIN_CLAUSE + ")")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId AND (" + ADMIN_CLAUSE + ")")
    Page<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
                                              @Param("entityId") Long entityId, Pageable pageable);

    // Internal lookups (e.g. tests, services): not exposed over REST
    @RestResource(exported = false)
    List<AuditLog> findByAction(String action);
}
