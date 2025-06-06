package net.cycastic.portfoliotoolkit.application.project.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.dto.ProjectDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectCommandHandler implements Command.Handler<GetProjectCommand, ProjectDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ProjectRepository projectRepository;

    @Override
    public ProjectDto handle(GetProjectCommand getProjectCommand) {
        var userId = loggedUserAccessor.getUserId();
        var project = projectRepository.findById(getProjectCommand.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Could not found project"));
        if (!loggedUserAccessor.isAdmin() &&
            !project.getUser().getId().equals(userId)){
            throw new ForbiddenException();
        }
        return ProjectDto.fromDomain(project);
    }
}
