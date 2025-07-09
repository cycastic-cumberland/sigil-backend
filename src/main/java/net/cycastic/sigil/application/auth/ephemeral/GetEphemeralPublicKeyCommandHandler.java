package net.cycastic.sigil.application.auth.ephemeral;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.PemDto;
import net.cycastic.sigil.service.impl.HashicorpVaultEphemeralAsymmetricEncryptionProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEphemeralPublicKeyCommandHandler implements Command.Handler<GetEphemeralPublicKeyCommand, PemDto> {
    private final HashicorpVaultEphemeralAsymmetricEncryptionProvider hashicorpVaultEphemeralAsymmetricEncryptionProvider;

    @Override
    public PemDto handle(GetEphemeralPublicKeyCommand command) {
        return hashicorpVaultEphemeralAsymmetricEncryptionProvider.getPublicKey();
    }
}
