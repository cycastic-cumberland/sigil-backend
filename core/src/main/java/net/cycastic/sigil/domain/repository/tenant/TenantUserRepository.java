package net.cycastic.sigil.domain.repository.tenant;

import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.TenantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantUserRepository extends JpaRepository<TenantUser, Integer> {
    interface TenantUserItem {
        String getFirstName();
        String getLastName();
        String getEmail();
        int getMembership();
        int getPermissions();
    }

    boolean existsByTenant_IdAndUser_Id(int tenantId, int userId);
    boolean existsByTenantAndUser_Id(Tenant tenant, int userId);

    Optional<TenantUser> findByTenant_IdAndUser_Id(int tenantId, int userId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenant.id = :tenantId AND tu.user.normalizedEmail = UPPER(:email)")
    Optional<TenantUser> findByTenantIdAndUserEmail(@Param("tenantId") int tenantId, @Param("email") String email);

    @Query(value = """
                   SELECT tu.user.email AS email,
                          tu.user.lastName AS lastName,
                          tu.user.firstName AS firstName,
                          CASE WHEN tu.tenant.owner = tu.user THEN 0 WHEN tu.isModerator THEN 1 ELSE 2 END AS membership,
                          tu.permissions AS permissions
                   FROM TenantUser tu
                   WHERE tu.lastInvited IS NULL AND
                         tu.tenant.id = :tenantId AND
                         tu.user.id <> :excludeUserId AND
                         (tu.user.normalizedEmail ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                            tu.user.firstName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                            tu.user.lastName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\')
                   """,
            countQuery = """

                    SELECT COUNT(tu) FROM TenantUser tu WHERE tu.lastInvited IS NULL AND
                                                              tu.tenant.id = :tenantId AND
                                                              tu.user.id <> :excludeUserId AND
                                                              (tu.user.normalizedEmail ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                                                                 tu.user.firstName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\' OR
                                                                 tu.user.lastName ILIKE CONCAT(:contentTerm, '%') ESCAPE '\\') AND
                                                              tu.lastInvited IS NULL
                    """)
    Page<TenantUserItem> findItemsByContentTerm(@Param("tenantId") int tenantId,
                                                @Param("contentTerm") String contentTerm,
                                                @Param("excludeUserId") Integer excludeUserId,
                                                Pageable pageable);

    @Query(value = """
                   SELECT tu.user.email AS email,
                          tu.user.lastName AS lastName,
                          tu.user.firstName AS firstName,
                          CASE WHEN tu.tenant.owner = tu.user THEN 0 WHEN tu.isModerator THEN 1 ELSE 2 END AS membership,
                          tu.permissions AS permissions
                   FROM TenantUser tu
                   WHERE tu.lastInvited IS NULL AND
                         tu.tenant.id = :tenantId
                   """,
            countQuery = """

                    SELECT COUNT(tu) FROM TenantUser tu WHERE tu.lastInvited IS NULL AND
                                                              tu.tenant.id = :tenantId
                    """)
    Page<TenantUserItem> findAllItems(@Param("tenantId") int tenantId, Pageable pageable);
}
