package net.cycastic.portfoliotoolkit.application.auth.extract.claims;

import an.awesome.pipelinr.Command;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExtractClaimsCommandHandler implements Command.Handler<ExtractClaimsCommand, Claims> {
    private final JwtVerifier jwtVerifier;

    @Override
    public Claims handle(ExtractClaimsCommand extractClaimsCommand) {
        return jwtVerifier.extractClaims(extractClaimsCommand.authToken());
    }
}
