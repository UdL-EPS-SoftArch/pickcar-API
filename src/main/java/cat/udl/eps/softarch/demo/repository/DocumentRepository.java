package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Document;
import cat.udl.eps.softarch.demo.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface DocumentRepository extends CrudRepository<Document, Long>, PagingAndSortingRepository<Document, Long> {

    String OWNERSHIP_CLAUSE =
        "d.uploadedBy.id = ?#{authentication.name} " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT d FROM Document d WHERE " + OWNERSHIP_CLAUSE)
    Page<Document> findAll(Pageable pageable);

    @Override
    @Query("SELECT d FROM Document d WHERE d.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<Document> findById(@Param("id") Long id);

    @Query("SELECT d FROM Document d WHERE d.uploadedBy = :user AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Document> findByUploadedBy(@Param("user") User uploader, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.entityType = :entityType AND d.entityId = :entityId AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Document> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
                                               @Param("entityId") Long entityId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.status = :status AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Document> findByStatus(@Param("status") Document.Status status, Pageable pageable);

    List<Document> findByFileName(String fileName);
}
