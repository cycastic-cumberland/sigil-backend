package net.cycastic.sigil.application.email.smtp.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.repository.EncryptedSmtpCredentialRepository;
import net.cycastic.sigil.domain.repository.ProjectRepository;
import net.cycastic.sigil.domain.dto.BaseSmtpCredentialDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuerySmtpCredentialsCommandHandler implements Command.Handler<QuerySmtpCredentialsCommand, PageResponseDto<BaseSmtpCredentialDto>> {
    private final EncryptedSmtpCredentialRepository encryptedSmtpCredentialRepository;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PageResponseDto<BaseSmtpCredentialDto> handle(QuerySmtpCredentialsCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(ForbiddenException::new);
        if (!loggedUserAccessor.isAdmin() && !project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        var page = encryptedSmtpCredentialRepository.findByProject(project, command.toPageable());
        return PageResponseDto.fromDomain(page, BaseSmtpCredentialDto::new);
    }
}
