package net.cycastic.portfoliotoolkit.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.EncryptedSmtpCredential;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncryptedSmtpCredentialRepository extends JpaRepository<EncryptedSmtpCredential, Integer> {
    Page<EncryptedSmtpCredential> findByProject(@NotNull Project project, Pageable pageable);
}
