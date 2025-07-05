package net.cycastic.sigil.application.project.save;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Project;
import net.cycastic.sigil.domain.repository.ProjectRepository;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.LimitProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class SaveProjectCommandHandler implements Command.Handler<SaveProjectCommand, IdDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LimitProvider limitProvider;

    private Integer createProject(SaveProjectCommand command){
        var user = userRepository.findByIdForUpdate(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var limit = limitProvider.extractUsageDetails(user);
        if (limit.getProjectCount() != null){
            var currentProjectCount = projectRepository.countByUser(user);
            if (currentProjectCount >= limit.getProjectCount()){
                throw new ForbiddenException("Current user has reached project limit");
            }
        }

        var project = Project.builder()
                .projectName(command.getProjectName())
                .corsSettings(command.getCorsSettings())
                .user(user)
                .createdAt(OffsetDateTime.now())
                .build();
        projectRepository.save(project);
        return project.getId();
    }

    private Integer updateProject(SaveProjectCommand command, Project project){
        project.setProjectName(command.getProjectName());
        project.setCorsSettings(command.getCorsSettings() == null
                ? null : command.getCorsSettings().isEmpty()
                ? null : command.getCorsSettings());
        project.setUpdatedAt(OffsetDateTime.now());

        projectRepository.save(project);
        return project.getId();
    }

    @Override
    @Transactional
    public IdDto handle(SaveProjectCommand command) {
        if (command.getId() == null){
            var userId = command.getUserId();
            if (userId == null){
                command.setUserId(loggedUserAccessor.getUserId());
            } else if (!loggedUserAccessor.isAdmin() &&
                    userId != loggedUserAccessor.getUserId()){
                throw new ForbiddenException();
            }
            return new IdDto(createProject(command));
        }

        var userId = loggedUserAccessor.getUserId();
        var project = projectRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find project"));
        if (!loggedUserAccessor.isAdmin() &&
                !project.getUser().getId().equals(userId)){
            throw new ForbiddenException();
        }

        return new IdDto(updateProject(command, project));
    }
}
