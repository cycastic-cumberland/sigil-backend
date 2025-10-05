package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.oidc.configs.GetOidcConfigurationCommand;
import net.cycastic.sigil.application.oidc.jwks.GetJwksCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.domain.dto.auth.JwksDto;
import net.cycastic.sigil.domain.dto.auth.OidcConfigurationDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(".well-known")
public class WellknownController {
    private static final String CACHE_KEY = "WellknownController";
    private final Pipelinr pipelinr;

    @GetMapping("openid-configuration")
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'openid-configuration'")
    public OidcConfigurationDto getOidcConfiguration(){
        return pipelinr.send(GetOidcConfigurationCommand.INSTANCE);
    }

    @GetMapping("jwks.json")
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'jwks.json'")
    public JwksDto getJwks(){
        return pipelinr.send(GetJwksCommand.INSTANCE);
    }
}
