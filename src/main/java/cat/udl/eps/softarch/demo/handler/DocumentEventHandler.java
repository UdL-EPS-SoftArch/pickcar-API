package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.Document;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DocumentRepository;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class DocumentEventHandler {
    final DocumentRepository documentRepository;

    public DocumentEventHandler(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @HandleBeforeCreate
    public void handleDocumentPreCreate(Document document) {
        User uploader = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        document.setUploadedBy(uploader);
        document.setStatus(Document.Status.UPLOADED);
    }

    // Stored documents are immutable for their uploader; only an admin can review them
    @HandleBeforeSave
    public void handleDocumentPreSave(Document document) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Only an admin can modify a document");
        }
    }
}
