package net.cycastic.sigil.domain.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Locale;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = "wrappedUserKey")
    Optional<User> findByNormalizedEmail(@NotNull String normalizedEmail);

    @Query("SELECT tu.user FROM TenantUser tu WHERE tu.tenant.id = :tenantId AND tu.user.normalizedEmail = UPPER(:normalizedEmail)")
    Optional<User> findByEmailAndTenantId(@Param("normalizedEmail") @NotNull String normalizedEmail, @Param("tenantId") @NotNull int tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Integer id);

    default Optional<User> getByEmail(@NotNull String email){
        return findByNormalizedEmail(email.toUpperCase(Locale.ROOT));
    }
}
