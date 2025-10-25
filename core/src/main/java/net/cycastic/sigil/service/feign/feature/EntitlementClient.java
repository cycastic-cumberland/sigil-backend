package net.cycastic.sigil.service.feign.feature;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "feature-server",
        url = "${application.feign.client.feature.base-url}/api/entitlements"
)
public interface EntitlementClient extends EntitlementApi {
    @Override
    @GetMapping("by-tenant")
    EntitlementDto getEntitlement(@NotNull @RequestParam String entitlementType, @RequestParam int tenantId);

    @Override
    @PostMapping
    void saveEntitlement(@RequestBody EntitlementDto request);

    @DeleteMapping
    void deleteEntitlement(@NotNull @RequestParam String entitlementType, @RequestParam int tenantId);
}
