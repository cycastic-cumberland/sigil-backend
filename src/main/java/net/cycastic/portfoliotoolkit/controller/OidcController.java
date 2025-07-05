package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.oidc.configs.GetOidcConfigurationCommand;
import net.cycastic.portfoliotoolkit.application.oidc.jwks.GetJwksCommand;
import net.cycastic.portfoliotoolkit.domain.dto.JwksDto;
import net.cycastic.portfoliotoolkit.domain.dto.OidcConfigurationDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(".well-known")
public class OidcController {
    private final Pipelinr pipelinr;

    @GetMapping("openid-configuration")
    public OidcConfigurationDto getOidcConfiguration(){
        return pipelinr.send(GetOidcConfigurationCommand.INSTANCE);
    }

    @GetMapping("jwks.json")
    public JwksDto getJwks(){
        return pipelinr.send(GetJwksCommand.INSTANCE);
    }
}
