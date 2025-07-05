package net.cycastic.sigil.application.email.smtp.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.repository.EncryptedSmtpCredentialRepository;
import net.cycastic.sigil.domain.dto.DecryptedSmtpCredentialDto;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSmtpCredentialCommandHandler implements Command.Handler<GetSmtpCredentialCommand, DecryptedSmtpCredentialDto> {
    private final EncryptedSmtpCredentialRepository encryptedSmtpCredentialRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final DecryptionProvider decryptionProvider;

    @Override
    public DecryptedSmtpCredentialDto handle(GetSmtpCredentialCommand command) {
        var credential = encryptedSmtpCredentialRepository.findById(command.getId())
                .orElseThrow(ForbiddenException::new);
        if (!loggedUserAccessor.isAdmin() &&
                !credential.getProject().getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        var address = decryptionProvider.decrypt(credential.getFromAddress());
        var password = decryptionProvider.decrypt(credential.getPassword());
        return new DecryptedSmtpCredentialDto(credential, address, password);
    }
}
