package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.Cipher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CipherRepository extends JpaRepository<Cipher, Long> {
    @Query("SELECT wc.wrappedUserKey FROM WebAuthnCredential wc WHERE wc.id = :id")
    Optional<Cipher> findByWebAuthnCredentialId(@Param("id") int id);

    @Query("SELECT u.wrappedUserKey FROM User u WHERE u.id = :id")
    Optional<Cipher> findByWrappedUserKey(@Param("id") int id);
}
