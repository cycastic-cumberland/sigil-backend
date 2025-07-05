package net.cycastic.sigil.application.project.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.ProjectRepository;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.domain.dto.ProjectDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.LoggedUserAccessor;
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
        if (userId != null &&
                !loggedUserAccessor.isAdmin() &&
                !userId.equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        if (userId == null){
            userId = loggedUserAccessor.getUserId();
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RequestException(404, "Could not find user"));
        var projects = projectRepository.findProjectsByUser(user, queryProjectsCommand.toPageable());
        return PageResponseDto.fromDomain(projects, ProjectDto::fromDomain);
    }
}
