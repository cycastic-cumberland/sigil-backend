package net.cycastic.sigil.controller;

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
public class FeaturesController {
    private final FeaturesClient featuresClient;

    @GetMapping("feature")
    public FeatureDto getFeature(BaseGetFeatureCommand command){
        return featuresClient.getFeature(command.getFeatureId(), command.getTenantId());
    }
}
