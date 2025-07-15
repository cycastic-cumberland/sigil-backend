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
    interface TenantUserEmailItem {
        String getEmail();
    }

    boolean existsByTenant_IdAndUser_Id(int tenantId, int userId);
    boolean existsByTenantAndUser_Id(Tenant tenant, int userId);

    Optional<TenantUser> findByTenant_IdAndUser_Id(int tenantId, int userId);

    @Query(value = """
                   SELECT tu.user.email AS email
                   FROM TenantUser tu WHERE tu.tenant.id = :tenantId AND tu.user.id <> :excludeUserId AND tu.user.normalizedEmail ILIKE CONCAT(:emailPrefix, '%') ESCAPE '\\'
                   """,
            countQuery = "SELECT COUNT(tu) FROM TenantUser tu WHERE tu.tenant.id = :tenantId AND tu.user.id <> :excludeUserId AND tu.user.normalizedEmail ILIKE CONCAT(:emailPrefix, '%') ESCAPE '\\'")
    Page<TenantUserEmailItem> findByEmailPrefix(@Param("tenantId") int tenantId,
                                                @Param("emailPrefix") String emailPrefix,
                                                @Param("excludeUserId") int excludeUserId,
                                                Pageable pageable);
}
