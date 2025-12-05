package net.cycastic.sigil.application.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.domain.dto.tenant.SlimTenantUserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachedTenantService {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BooleanStrongBox {
        private boolean value;
    }

    private static final String CACHE_KEY = "CachedTenantService";

    private final PartitionUserRepository partitionUserRepository;
    private final TenantUserRepository tenantUserRepository;

    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getTenantUser' + '?tenantId=' + #tenantId + '&userId=' + #userId",
            unless = "#result == null")
    public SlimTenantUserDto getTenantUserSlim(int tenantId, int userId){
        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(tenantId, userId)
                .orElseThrow(RequestException::forbidden);

        return SlimTenantUserDto.builder()
                .lastInvited(tenantUser.getLastInvited())
                .build();
    }

    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'isUserInPartition' + '?tenantId=' + #tenantId  + '&partitionId=' + #partitionId + '&userId=' + #userId",
            unless = "#result.value == false")
    public BooleanStrongBox isUserInPartition(int tenantId, int partitionId, int userId){
        var value = partitionUserRepository.existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(tenantId,
                partitionId,
                userId);
        return new BooleanStrongBox(value);
    }
}
