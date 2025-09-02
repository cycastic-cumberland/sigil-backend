package net.cycastic.sigil.application.user.webauthn.enroll;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;

@TransactionalCommand
public class EnrollWebAuthnEnvelopCommand extends WebAuthnCredentialDto implements Command<Void> {
}
