package net.cycastic.sigil.domain.repository.tenant;

import net.cycastic.sigil.domain.model.listing.PartitionUser;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    interface TenantQueryItem {
        int getId();
        String getTenantName();
        long getAccumulatedAttachmentStorageUsage();
        int getMembership();
        int getPermissions();
        OffsetDateTime getCreatedAt();
        OffsetDateTime getUpdatedAt();
    }

    @Query(value = "SELECT al.listing.partition.tenant FROM AttachmentListing al WHERE al = :attachmentListing")
    Optional<Tenant> findByAttachmentListing(@Param("attachmentListing") AttachmentListing attachmentListing);

    @Query(value = "SELECT pu.partition.tenant FROM PartitionUser pu WHERE pu = :partitionUser")
    Tenant findByPartitionUser(@Param("partitionUser") PartitionUser partitionUser);

    @Query(value = "SELECT p.tenant FROM Partition p WHERE p.id = :partitionId")
    Tenant findByPartitionId(@Param("partitionId") int partitionId);

    @Query(value = """
                   SELECT tu.tenant.id as id,
                          tu.tenant.name as tenantName,
                          tu.tenant.accumulatedAttachmentStorageUsage as accumulatedAttachmentStorageUsage,
                          CASE WHEN tu.tenant.owner = tu.user THEN 0 WHEN tu.isModerator THEN 1 ELSE 2 END AS membership,
                          tu.permissions as permissions,
                          tu.tenant.createdAt as createdAt,
                          tu.tenant.updatedAt as updatedAt
                   FROM TenantUser tu WHERE tu.user = :user
                   """,
            countQuery = "SELECT COUNT(tu) FROM TenantUser tu WHERE tu.user = :user")
    Page<TenantQueryItem> findByUser(@Param("user") User user, Pageable pageable);

    @Query(value = """
                   SELECT tu.tenant.id as id,
                          tu.tenant.name as tenantName,
                          tu.tenant.accumulatedAttachmentStorageUsage as accumulatedAttachmentStorageUsage,
                          CASE WHEN tu.tenant.owner = tu.user THEN 0 WHEN tu.isModerator THEN 1 ELSE 2 END AS membership,
                          tu.permissions as permissions,
                          tu.tenant.createdAt as createdAt,
                          tu.tenant.updatedAt as updatedAt
                   FROM TenantUser tu WHERE tu.tenant = :tenant AND tu.user = :user
                   """,
            countQuery = "SELECT COUNT(tu) FROM TenantUser tu WHERE tu.tenant = :tenant AND tu.user = :user")
    Optional<TenantQueryItem> findByTenantAndUser(@Param("tenant") Tenant tenant, @Param("user") User user);
}
