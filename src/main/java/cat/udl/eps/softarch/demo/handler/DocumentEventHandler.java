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
        // File type validation
        String contentType = document.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Invalid file type: " + contentType);
        }
        
        // File size validation (max 10 MB)
        Long fileSize = document.getFileSize();
        if (fileSize == null || fileSize > 10_485_760) { // 10 MB in bytes
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 10 MB");
        }
        
        User uploader = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        document.setUploadedBy(uploader);
        document.setStatus(Document.Status.UPLOADED);
    }
    
    private boolean isAllowedContentType(String contentType) {
        // Allow common image and PDF file types
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
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
