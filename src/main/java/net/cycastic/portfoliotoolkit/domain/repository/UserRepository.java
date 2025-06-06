package net.cycastic.portfoliotoolkit.domain.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Locale;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Nullable User findByNormalizedEmail(@NotNull String normalizedEmail);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT * FROM users u WHERE u.normalized_email = UPPER(:email)")
//    @Nullable User findByEmailForUpdate(@Param("email") String email);

    default @Nullable User getByEmail(@NotNull String email){
        return findByNormalizedEmail(email.toUpperCase(Locale.ROOT));
    }
}
