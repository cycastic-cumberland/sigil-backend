package net.cycastic.sigil.domain.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CipherRepository extends JpaRepository<Cipher, Long> {
}
