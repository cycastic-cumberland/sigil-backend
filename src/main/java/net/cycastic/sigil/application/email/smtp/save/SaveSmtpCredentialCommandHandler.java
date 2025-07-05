package net.cycastic.sigil.application.email.smtp.save;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.EncryptedSmtpCredential;
import net.cycastic.sigil.domain.repository.EncryptedSmtpCredentialRepository;
import net.cycastic.sigil.domain.repository.ProjectRepository;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveSmtpCredentialCommandHandler implements Command.Handler<SaveSmtpCredentialCommand, IdDto> {
    private final EncryptedSmtpCredentialRepository encryptedSmtpCredentialRepository;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final EncryptionProvider encryptionProvider;

    @Override
    public IdDto handle(SaveSmtpCredentialCommand command) {
        EncryptedSmtpCredential credential;
        if (command.getId() == null){
            var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                    .orElseThrow(ForbiddenException::new);

            credential = EncryptedSmtpCredential.builder()
                    .project(project)
                    .serverAddress(command.getServerAddress())
                    .secureSmtp(command.getSecureSmtp())
                    .port(command.getPort())
                    .timeout(command.getTimeout())
                    .fromAddress(encryptionProvider.encrypt(command.getFromAddress()))
                    .fromName(command.getFromName())
                    .password(encryptionProvider.encrypt(command.getPassword()))
                    .build();
        } else {
            credential = encryptedSmtpCredentialRepository.findById(command.getId())
                    .orElseThrow(() -> new RequestException(404, "Credential not found"));
            if (!loggedUserAccessor.isAdmin() &&
                    !credential.getProject().getUser().getId().equals(loggedUserAccessor.getUserId())){
                throw new ForbiddenException();
            }

            credential.setServerAddress(command.getServerAddress());
            credential.setSecureSmtp(command.getSecureSmtp());
            credential.setPort(command.getPort());
            credential.setTimeout(command.getTimeout());
            credential.setFromAddress(encryptionProvider.encrypt(command.getFromAddress()));
            credential.setFromName(command.getFromName());
            credential.setPassword(encryptionProvider.encrypt(command.getPassword()));
        }

        encryptedSmtpCredentialRepository.save(credential);
        return new IdDto(credential.getId());
    }
}
