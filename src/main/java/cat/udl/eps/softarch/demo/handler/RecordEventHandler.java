package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.Record;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.RecordRepository;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class RecordEventHandler {
    final RecordRepository recordRepository;

    public RecordEventHandler(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @HandleBeforeCreate
    public void handleRecordPreCreate(Record record) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return; // internal/system create: keep the owner assigned by the caller
        }
        record.setOwnedBy((User) authentication.getPrincipal());
        if (record.getStatus() == null) {
            record.setStatus(Record.Status.PRIVATE);
        }
    }

    @HandleBeforeSave
    public void handleRecordPreSave(Record record) {
        checkOwnerOrAdmin(record);
    }

    @HandleBeforeDelete
    public void handleRecordPreDelete(Record record) {
        checkOwnerOrAdmin(record);
    }

    private void checkOwnerOrAdmin(Record record) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return; // internal/system call
        }
        User owner = record.getOwnedBy();
        if (owner != null && owner.getUsername().equals(authentication.getName())) {
            return;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return;
        }
        throw new AccessDeniedException("Only the owner or an admin can modify the record");
    }
}
