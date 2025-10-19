package net.cycastic.sigil.application.feature.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.FeatureDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFeatureCommandHandler implements Command.Handler<GetFeatureCommand, FeatureDto> {
    @Override
    public FeatureDto handle(GetFeatureCommand command) {
        return FeatureDto.builder()
                .featureId(command.getFeatureId())
                .tenantId(command.getTenantId())
                .build();
    }
}
