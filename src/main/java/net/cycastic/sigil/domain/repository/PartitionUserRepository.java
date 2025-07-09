package net.cycastic.sigil.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.PartitionUser;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartitionUserRepository extends JpaRepository<PartitionUser, Integer> {
    Optional<PartitionUser> findByPartition_IdAndUser_Id(int partitionId, int userId);

    boolean existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(@NotNull int partitionTenantId, int partitionId, int userId);

    @Query("SELECT al.listing.partition FROM AttachmentListing al WHERE al = :attachmentListing")
    Optional<PartitionUser> findByAttachmentListing(@Param("attachmentListing") AttachmentListing listing);
}
