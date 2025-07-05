package net.cycastic.portfoliotoolkit.application.oidc.configs;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.domain.dto.OidcConfigurationDto;

public class GetOidcConfigurationCommand implements Command<OidcConfigurationDto> {
    public static final GetOidcConfigurationCommand INSTANCE = new GetOidcConfigurationCommand();
}
