package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.ProjectRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectValidator implements CommandValidator{
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public void validate(Command command) {
        var projectIdOpt = loggedUserAccessor.tryGetProjectId();
        if (projectIdOpt.isEmpty()){
            return;
        }

        var project = projectRepository.findById(projectIdOpt.get())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        if (!loggedUserAccessor.isAdmin() && !project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean matches(Command command) {
        return true;
    }
}
