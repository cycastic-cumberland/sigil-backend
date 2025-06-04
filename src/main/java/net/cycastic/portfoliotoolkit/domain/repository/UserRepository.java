package net.cycastic.portfoliotoolkit.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

import java.util.Locale;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Nullable User findByNormalizedEmail(@NotNull String normalizedEmail);

    default @Nullable User getByEmail(@NotNull String email){
        return findByNormalizedEmail(email.toUpperCase(Locale.ROOT));
    }
}
