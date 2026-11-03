package cat.udl.eps.softarch.demo.handler;

import cat.udl.eps.softarch.demo.domain.DriverProfile;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class DriverProfileEventHandler {

    final DriverProfileRepository entityRepository;

    @HandleBeforeCreate
    public void handleDriverProfilePreCreate(DriverProfile entity) {
        User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        entity.setUser(owner);
    }

    @HandleBeforeSave
    public void handleDriverProfilePreSave(DriverProfile entity) {
        User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!owner.getUsername().equals(entity.getUser().getUsername())) {
            throw new SecurityException("Only owner can modify");
        }
    }
}
