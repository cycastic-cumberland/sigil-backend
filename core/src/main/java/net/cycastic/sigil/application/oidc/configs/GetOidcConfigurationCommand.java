package net.cycastic.sigil.application.oidc.configs;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.auth.OidcConfigurationDto;

public class GetOidcConfigurationCommand implements Command<OidcConfigurationDto> {
    public static final GetOidcConfigurationCommand INSTANCE = new GetOidcConfigurationCommand();
}
