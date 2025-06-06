package net.cycastic.portfoliotoolkit.application.project.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.dto.ProjectDto;
import net.cycastic.portfoliotoolkit.dto.paging.PageResponseDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryProjectsCommandHandler implements Command.Handler<QueryProjectsCommand, PageResponseDto<ProjectDto>> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PageResponseDto<ProjectDto> handle(QueryProjectsCommand queryProjectsCommand) {
        var userId = queryProjectsCommand.getUserId();
        if (!loggedUserAccessor.getRoles().contains(ApplicationConstants.Roles.ADMIN) &&
                userId != loggedUserAccessor.getUserId()){
            throw new ForbiddenException();
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RequestException(404, "Could not found user"));
        var projects = projectRepository.findProjectsByUser(user, queryProjectsCommand.toPageable());
        return PageResponseDto.fromDomain(projects, ProjectDto::fromDomain);
    }
}
