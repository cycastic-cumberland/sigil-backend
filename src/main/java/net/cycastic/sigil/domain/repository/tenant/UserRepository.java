package net.cycastic.sigil.domain.repository.tenant;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.tenant.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Locale;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    interface KdfDetails {
        byte[] getParameters();
        byte[] getSalt();
    }

    interface PasswordBasedKdfDetails extends KdfDetails {
        byte[] getIv();
        byte[] getCipher();
    }

    interface WebAuthnBasedKdfDetails extends PasswordBasedKdfDetails {
        byte[] getWebAuthnCredentialId();
        byte[] getWebAuthnSalt();
        String getWebAuthnTransports();
    }

    Optional<User> findByNormalizedEmail(@NotNull String normalizedEmail);

    @Query("SELECT tu.user FROM TenantUser tu WHERE tu.tenant.id = :tenantId AND tu.user.normalizedEmail = UPPER(:email) AND tu.lastInvited IS NULL")
    Optional<User> findByEmailAndTenantId(@Param("email") @NotNull String email, @Param("tenantId") @NotNull int tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Integer id);

    default Optional<User> getByEmail(@NotNull String email){
        return findByNormalizedEmail(email.toUpperCase(Locale.ROOT));
    }

    @Query("""
           SELECT u.kdfSettings AS parameters,
                  u.kdfSalt AS salt,
                  u.wrappedUserKey.iv AS iv,
                  COALESCE(u.wrappedUserKey.cipherLong, u.wrappedUserKey.cipherStandard) AS cipher
                  FROM User u WHERE u.id = :userId
           """)
    Optional<PasswordBasedKdfDetails> getPasswordBasedKdfDetails(@Param("userId") int userId);

    @Query("""
           SELECT u.kdfSettings AS parameters,
                  u.kdfSalt AS salt,
                  u.webAuthnCredential.wrappedUserKey.iv AS iv,
                  COALESCE(u.webAuthnCredential.wrappedUserKey.cipherLong, u.webAuthnCredential.wrappedUserKey.cipherStandard) AS cipher,
                  u.webAuthnCredential.credentialId AS webAuthnCredentialId,
                  u.webAuthnCredential.salt AS webAuthnSalt,
                  u.webAuthnCredential.transports AS webAuthnTransports
                  FROM User u WHERE u.id = :userId
           """)
    Optional<WebAuthnBasedKdfDetails> getWebAuthnBasedKdfDetails(@Param("userId") int userId);
}
