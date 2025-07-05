package net.cycastic.portfoliotoolkit.application.oidc.configs;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.configuration.auth.JwtConfiguration;
import net.cycastic.portfoliotoolkit.domain.dto.OidcConfigurationDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class GetOidcConfigurationCommandHandler implements Command.Handler<GetOidcConfigurationCommand, OidcConfigurationDto> {
    private final JwtConfiguration jwtConfiguration;

    @Override
    public OidcConfigurationDto handle(GetOidcConfigurationCommand command) {
        if (jwtConfiguration.getIssuer() == null){
            throw new RequestException(500, "Issuer not defined");
        }

        return OidcConfigurationDto.builder()
                .issuer(jwtConfiguration.getIssuer())
                .jwksUri(URI.create(jwtConfiguration.getIssuer()).resolve("/.well-known/jwks.json").toString())
                .build();
    }
}
