package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.feature.get.GetFeatureCommand;
import net.cycastic.sigil.domain.dto.FeatureDto;
import net.cycastic.sigil.service.feign.feature.FeaturesClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/features")
public class FeaturesController implements FeaturesClient {
    private final Pipelinr pipelinr;

    @Override
    @GetMapping("feature")
    public FeatureDto getFeature(String featureId, @Nullable Integer tenantId) {
       return pipelinr.send(GetFeatureCommand.builder()
               .featureId(featureId)
               .tenantId(tenantId)
               .build());
    }
}
