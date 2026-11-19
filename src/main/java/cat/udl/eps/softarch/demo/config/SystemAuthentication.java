package cat.udl.eps.softarch.demo.config;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/** Runs internal repository operations with an explicit system (admin) identity. */
public final class SystemAuthentication {

    private SystemAuthentication() {}

    public static <T> T runAsSystem(Supplier<T> operation) {
        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "system", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        SecurityContextHolder.setContext(context);
        try {
            return operation.get();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }
}
