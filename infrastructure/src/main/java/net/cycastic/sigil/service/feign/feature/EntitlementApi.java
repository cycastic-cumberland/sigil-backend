package net.cycastic.sigil.service.feign.feature;

import net.cycastic.sigil.domain.dto.EntitlementDto;
import net.cycastic.sigil.domain.dto.paging.EnumerablePage;

public interface EntitlementApi {
    EntitlementDto getEntitlement(String entitlementType, int tenantId);
    EnumerablePage<EntitlementDto> listEntitlements(int tenantId, int pageSize);
    EnumerablePage<EntitlementDto> listEntitlements(int tenantId, int pageSize, String paginationToken);
    void saveEntitlement(EntitlementDto request);
    void deleteEntitlement(String entitlementType, int tenantId);
}
