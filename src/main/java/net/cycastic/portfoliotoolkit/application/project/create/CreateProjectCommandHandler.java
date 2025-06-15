package net.cycastic.portfoliotoolkit.application.project.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.dto.IdDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CreateProjectCommandHandler implements Command.Handler<CreateProjectCommand, IdDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private Integer createProject(CreateProjectCommand createProjectCommand){
        var user = userRepository.findByIdForUpdate(createProjectCommand.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        if (user.getProjectLimit() != null){
            var currentProjectCount = projectRepository.countByUser(user);
            if (currentProjectCount >= user.getProjectLimit()){
                throw new ForbiddenException("Current user has reached project limit");
            }
        }

        var project = Project.builder()
                .projectName(createProjectCommand.getProjectName())
                .corsSettings(createProjectCommand.getCorsSettings())
                .user(user)
                .createdAt(OffsetDateTime.now())
                .build();
        projectRepository.save(project);
        return project.getId();
    }

    @Override
    @Transactional
    public IdDto handle(CreateProjectCommand createProjectCommand) {
        var userId = createProjectCommand.getUserId();
        if (!loggedUserAccessor.isAdmin() &&
                userId != loggedUserAccessor.getUserId()){
            throw new ForbiddenException();
        }

        return new IdDto(createProject(createProjectCommand));
    }
}
