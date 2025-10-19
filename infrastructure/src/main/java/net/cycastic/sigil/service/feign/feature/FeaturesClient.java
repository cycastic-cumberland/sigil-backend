package net.cycastic.sigil.service.feign.feature;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.dto.FeatureDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "feature-server",
        url = "${application.feign.client.feature.base-url}/api/features"
)
public interface FeaturesClient {
    @RequestMapping(method = RequestMethod.GET, value = "feature")
    FeatureDto getFeature(@NotNull @RequestParam String featureId, @Nullable @RequestParam Integer tenantId);
}
