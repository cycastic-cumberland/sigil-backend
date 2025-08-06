package net.cycastic.sigil.application.user.webauthn.enroll;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;

public class EnrollWebAuthnEnvelopCommand extends WebAuthnCredentialDto implements Command<Void> {
}
