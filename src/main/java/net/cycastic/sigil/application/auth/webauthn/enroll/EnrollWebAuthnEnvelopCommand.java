package net.cycastic.sigil.application.auth.webauthn.enroll;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;

public class EnrollWebAuthnEnvelopCommand extends WebAuthnCredentialDto implements Command<Void> {
}
