package net.cycastic.portfoliotoolkit.application.project.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DeleteProjectCommandHandler implements Command.Handler<DeleteProjectCommand, @Null Object> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ProjectRepository projectRepository;

    @Override
    public @Null Object handle(DeleteProjectCommand command) {
        var userId = loggedUserAccessor.getUserId();
        var project = projectRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find project"));
        if (!loggedUserAccessor.isAdmin() &&
                !project.getUser().getId().equals(userId)){
            throw new ForbiddenException();
        }

        project.setRemovedAt(OffsetDateTime.now());
        projectRepository.save(project);
        return null;
    }
}
