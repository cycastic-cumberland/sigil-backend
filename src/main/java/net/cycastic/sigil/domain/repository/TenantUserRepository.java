package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.Tenant;
import net.cycastic.sigil.domain.model.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantUserRepository extends JpaRepository<TenantUser, Integer> {
    boolean existsByTenant_IdAndUser_Id(int tenantId, int userId);
    boolean existsByTenantAndUser_Id(Tenant tenant, int userId);

    Optional<TenantUser> findByTenant_IdAndUser_Id(int tenantId, int userId);
}
