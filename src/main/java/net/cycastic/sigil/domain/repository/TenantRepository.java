package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.PartitionUser;
import net.cycastic.sigil.domain.model.Tenant;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    @Query(value = "SELECT al.listing.partition.tenant FROM AttachmentListing al WHERE al = :attachmentListing")
    Optional<Tenant> findByAttachmentListing(@Param("attachmentListing") AttachmentListing attachmentListing);

    @Query(value = "SELECT pu.partition.tenant FROM PartitionUser pu WHERE pu = :partitionUser")
    Optional<Tenant> findByPartitionUser(@Param("partitionUser") PartitionUser partitionUser);

    @Query(value = "SELECT tu.tenant FROM TenantUser tu WHERE tu.user = :user",
            countQuery = "SELECT COUNT(tu) FROM TenantUser tu WHERE tu.user = :user")
    Page<Tenant> findByUser(@Param("user") User user, Pageable pageable);
}
