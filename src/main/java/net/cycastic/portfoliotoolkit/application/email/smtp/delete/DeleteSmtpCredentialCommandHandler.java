package net.cycastic.portfoliotoolkit.application.email.smtp.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.repository.EncryptedSmtpCredentialRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteSmtpCredentialCommandHandler implements Command.Handler<DeleteSmtpCredentialCommand, @Null Object> {
    private final EncryptedSmtpCredentialRepository encryptedSmtpCredentialRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public @Null Object handle(DeleteSmtpCredentialCommand command) {
        var credential = encryptedSmtpCredentialRepository.findById(command.getId())
                .orElseThrow(ForbiddenException::new);
        if (!loggedUserAccessor.isAdmin() &&
                !credential.getProject().getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        encryptedSmtpCredentialRepository.delete(credential);
        return null;
    }
}
