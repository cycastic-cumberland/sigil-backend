package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantUserRepository extends JpaRepository<TenantUser, Integer> {
    boolean existsByTenant_IdAndUser_Id(int projectId, int userId);
}
