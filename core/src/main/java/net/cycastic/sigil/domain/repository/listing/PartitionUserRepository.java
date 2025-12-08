package net.cycastic.sigil.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.PartitionUser;
import net.cycastic.sigil.domain.model.tenant.User;
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
        String getAvatarToken();
        int getPermissions();
    }

    Optional<PartitionUser> findByPartition_IdAndUser_Id(int partitionId, int userId);

    @Query("""
           SELECT pu.user FROM PartitionUser pu WHERE pu.partition.id = :partitionId AND pu.user.normalizedEmail = UPPER(:userEmail)
           """)
    Optional<User> findPartitionMemberByEmail(@Param("partitionId") int partitionId, @Param("userEmail") String userEmail);

    boolean existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(@NotNull int partitionTenantId, int partitionId, int userId);

    @Query("SELECT al.listing.partition FROM AttachmentListing al WHERE al = :attachmentListing")
    Optional<PartitionUser> findByAttachmentListing(@Param("attachmentListing") AttachmentListing listing);

    void removeByPartitionAndUser(Partition partition, User user);

    @Query(value = """
                   SELECT pu.user.firstName AS firstName,
                   pu.user.lastName AS lastName,
                   pu.user.email AS email,
                   pu.user.avatarToken AS avatarToken,
                   pu.permissions AS permissions
                   FROM PartitionUser pu WHERE pu.partition.id = :partitionId
                   """,
           countQuery = "SELECT COUNT(1) FROM PartitionUser pu WHERE pu.partition.id = :partitionId")
    Page<PartitionUserResult> queryByPartition(@Param("partitionId") int partitionId, Pageable pageable);

    @Query(value = """
                   SELECT pu.user.firstName AS firstName,
                   pu.user.lastName AS lastName,
                   pu.user.email AS email,
                   pu.user.avatarToken AS avatarToken,
                   pu.permissions AS permissions
                   FROM PartitionUser pu
                   WHERE pu.partition.id = :partitionId AND
                         (pu.user.normalizedEmail ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                             pu.user.firstName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                             pu.user.lastName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\')
                   """,
            countQuery = """
                         SELECT COUNT(1) FROM PartitionUser pu WHERE pu.partition.id = :partitionId AND
                                                  (pu.user.normalizedEmail ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                                                         pu.user.firstName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                                                         pu.user.lastName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\')
                         """)
    Page<PartitionUserResult> queryByPartition(@Param("partitionId") int partitionId,
                                               @Param("contentTerm") String contentTerm,
                                               Pageable pageable);


    @Query(value = """
                   SELECT pu.user.firstName AS firstName,
                   pu.user.lastName AS lastName,
                   pu.user.email AS email,
                   pu.permissions AS permissions
                   FROM PartitionUser pu WHERE pu.partition.id = :partitionId AND pu.user.id = :userId
                   """)
    Optional<PartitionUserResult> getByPartitionAndUser(@Param("partitionId") int partitionId, @Param("userId") int userId);
}
