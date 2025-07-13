package net.cycastic.sigil.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.Partition;
import net.cycastic.sigil.domain.model.PartitionUser;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartitionUserRepository extends JpaRepository<PartitionUser, Integer> {
    interface PartitionUserResult {
        String getFirstName();
        String getLastName();
        String getEmail();
        int getPermissions();
    }

    Optional<PartitionUser> findByPartition_IdAndUser_Id(int partitionId, int userId);

    boolean existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(@NotNull int partitionTenantId, int partitionId, int userId);

    @Query("SELECT al.listing.partition FROM AttachmentListing al WHERE al = :attachmentListing")
    Optional<PartitionUser> findByAttachmentListing(@Param("attachmentListing") AttachmentListing listing);

    void removeByPartitionAndUser(Partition partition, User user);

    @Query(value = """
                   SELECT pu.user.firstName AS firstName,
                   pu.user.lastName AS lastName,
                   pu.user.email AS email,
                   pu.permissions AS permissions
                   FROM PartitionUser pu WHERE pu.partition.id = :partitionId
                   """,
           countQuery = "SELECT COUNT(1) FROM PartitionUser pu WHERE pu.partition.id = :partitionId")
    Page<PartitionUserResult> queryByPartition(@Param("partitionId") int partitionId, Pageable pageable);


    @Query(value = """
                   SELECT pu.user.firstName AS firstName,
                   pu.user.lastName AS lastName,
                   pu.user.email AS email,
                   pu.permissions AS permissions
                   FROM PartitionUser pu WHERE pu.partition.id = :partitionId AND pu.user.id = :userId
                   """)
    Optional<PartitionUserResult> getByPartitionAndUser(@Param("partitionId") int partitionId, @Param("userId") int userId);
}
