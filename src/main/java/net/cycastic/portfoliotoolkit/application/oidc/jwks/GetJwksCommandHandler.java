package net.cycastic.portfoliotoolkit.application.oidc.jwks;

import an.awesome.pipelinr.Command;

import net.cycastic.portfoliotoolkit.domain.dto.JwksDto;
import net.cycastic.portfoliotoolkit.service.auth.AsymmetricJwtVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetJwksCommandHandler implements Command.Handler<GetJwksCommand, JwksDto> {
    private final Collection<AsymmetricJwtVerifier> asymmetricJwtVerifiers;

    @Autowired
    public GetJwksCommandHandler(List<AsymmetricJwtVerifier> asymmetricJwtVerifiers) {
        this.asymmetricJwtVerifiers = new HashSet<>(asymmetricJwtVerifiers);
    }

    @Override
    public JwksDto handle(GetJwksCommand command) {
        return new JwksDto(asymmetricJwtVerifiers.stream()
                .map(AsymmetricJwtVerifier::getJwk)
                .toList());
    }
}
