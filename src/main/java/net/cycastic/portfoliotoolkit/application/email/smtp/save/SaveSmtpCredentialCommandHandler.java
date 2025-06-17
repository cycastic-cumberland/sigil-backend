package net.cycastic.portfoliotoolkit.application.email.smtp.save;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.EncryptedSmtpCredential;
import net.cycastic.portfoliotoolkit.domain.repository.EncryptedSmtpCredentialRepository;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.dto.IdDto;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SaveSmtpCredentialCommandHandler implements Command.Handler<SaveSmtpCredentialCommand, IdDto> {
    private static final Set<String> VALID_SECURITY_SETTINGS;
    private static final Pattern HOSTNAME_PATTERN;
    private static final Pattern EMAIL_PATTERN;

    static {
        HOSTNAME_PATTERN = Pattern.compile(
                "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*\\.?$"
        );

        EMAIL_PATTERN = Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

        VALID_SECURITY_SETTINGS = new HashSet<>();
        VALID_SECURITY_SETTINGS.add("starttls");
        VALID_SECURITY_SETTINGS.add("none");
    }

    private final EncryptedSmtpCredentialRepository encryptedSmtpCredentialRepository;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final EncryptionProvider encryptionProvider;

    @Override
    public IdDto handle(SaveSmtpCredentialCommand command) {
        EncryptedSmtpCredential credential;
        var secureSmtp = command.getSecureSmtp().toLowerCase(Locale.ROOT);
        if (!VALID_SECURITY_SETTINGS.contains(secureSmtp)){
            throw new RequestException(400, "Invalid security settings");
        }
        if (!HOSTNAME_PATTERN.matcher(command.getServerAddress()).matches()){
            throw new RequestException(400, "Invalid server address");
        }
        if (!EMAIL_PATTERN.matcher(command.getFromAddress()).matches()){
            throw new RequestException(400, "Invalid sender address");
        }
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
