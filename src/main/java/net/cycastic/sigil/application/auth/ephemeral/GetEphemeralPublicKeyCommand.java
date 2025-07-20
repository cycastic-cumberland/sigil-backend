package net.cycastic.sigil.application.auth.ephemeral;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.auth.PemDto;

public class GetEphemeralPublicKeyCommand implements Command<PemDto> {
    public static final GetEphemeralPublicKeyCommand INSTANCE = new GetEphemeralPublicKeyCommand();
}
