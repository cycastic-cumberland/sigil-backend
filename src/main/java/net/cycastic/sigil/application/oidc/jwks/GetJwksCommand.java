package net.cycastic.sigil.application.oidc.jwks;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.JwksDto;

public class GetJwksCommand implements Command<JwksDto> {
    public static final GetJwksCommand INSTANCE = new GetJwksCommand();
}
