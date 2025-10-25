package net.cycastic.sigil.service.feign.feature;

import net.cycastic.sigil.domain.dto.EntitlementDto;

public interface EntitlementApi {
    EntitlementDto getEntitlement(String entitlementType, int tenantId);
    void saveEntitlement(EntitlementDto request);
    void deleteEntitlement(String entitlementType, int tenantId);
}
