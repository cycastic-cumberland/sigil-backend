package net.cycastic.sigil.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.EncryptedSmtpCredential;
import net.cycastic.sigil.domain.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncryptedSmtpCredentialRepository extends JpaRepository<EncryptedSmtpCredential, Integer> {
    Page<EncryptedSmtpCredential> findByProject(@NotNull Project project, Pageable pageable);
}
