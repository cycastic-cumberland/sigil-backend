package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.dto.keyring.interfaces.PartitionKeyMaterial;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CipherRepository extends JpaRepository<Cipher, Long> {
    @Query("SELECT wc.wrappedUserKey FROM WebAuthnCredential wc WHERE wc.id = :id")
    Optional<Cipher> findByWebAuthnCredentialId(@Param("id") int id);

    @Query("SELECT u.wrappedUserKey FROM User u WHERE u.id = :id")
    Optional<Cipher> findByWrappedUserKey(@Param("id") int id);

    @Query("""
           SELECT pu.partition.id AS partitionId,
                  pu.partitionUserKey.id AS cipherId,
                  COALESCE(pu.partitionUserKey.cipherLong, pu.partitionUserKey.cipherStandard) AS cipher,
                  pu.partitionUserKey.iv AS iv
           FROM PartitionUser pu WHERE pu.user.id = :userId AND pu.partition.tenant = :tenant AND pu.partitionUserKey IS NOT NULL
           """)
    List<PartitionKeyMaterial> getKeyringByUserIdAndTenant(@Param("userId") int userId, @Param("tenant") Tenant tenant);

    @Query("""
           SELECT pu.partition.id AS partitionId,
                  pu.partitionUserKey.id AS cipherId,
                  COALESCE(pu.partitionUserKey.cipherLong, pu.partitionUserKey.cipherStandard) AS cipher,
                  pu.partitionUserKey.iv AS iv
           FROM PartitionUser pu WHERE pu.user.id = :userId AND pu.partition = :partition AND pu.partitionUserKey IS NOT NULL
           """)
    Optional<PartitionKeyMaterial> getKeyByUserIdAndPartition(@Param("userId") int userId, @Param("partition") Partition partition);
}
