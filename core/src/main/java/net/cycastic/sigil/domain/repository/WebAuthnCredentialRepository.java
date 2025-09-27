package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.WebAuthnCredential;
import net.cycastic.sigil.domain.model.tenant.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Integer> {
    boolean existsByUser(User user);
    Optional<WebAuthnCredential> findByUser_Id(int userId);
}
