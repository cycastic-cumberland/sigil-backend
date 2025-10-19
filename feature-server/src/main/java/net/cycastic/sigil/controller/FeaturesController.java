package net.cycastic.sigil.controller;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.feature.get.BaseGetFeatureCommand;
import net.cycastic.sigil.domain.dto.FeatureDto;
import net.cycastic.sigil.service.feign.feature.FeaturesClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/features")
public class FeaturesController implements FeaturesClient {
    @Override
    @GetMapping("feature")
    public FeatureDto getFeature(String featureId, @Nullable Integer tenantId) {
        return FeatureDto.builder()
                .featureId(featureId)
                .tenantId(tenantId)
                .build();
    }
}
