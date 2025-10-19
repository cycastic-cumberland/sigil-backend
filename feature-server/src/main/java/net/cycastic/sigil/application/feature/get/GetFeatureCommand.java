package net.cycastic.sigil.application.feature.get;

import an.awesome.pipelinr.Command;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.FeatureDto;

@SuperBuilder
public class GetFeatureCommand extends BaseGetFeatureCommand implements Command<FeatureDto> {
}
