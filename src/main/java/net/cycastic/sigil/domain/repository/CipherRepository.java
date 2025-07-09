package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.Cipher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CipherRepository extends JpaRepository<Cipher, Long> {
}
