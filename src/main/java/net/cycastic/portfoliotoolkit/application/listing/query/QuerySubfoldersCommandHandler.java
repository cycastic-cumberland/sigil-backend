package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.dto.SubfoldersDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class QuerySubfoldersCommandHandler implements Command.Handler<QuerySubfoldersCommand, SubfoldersDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ProjectRepository projectRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    @Override
    public SubfoldersDto handle(QuerySubfoldersCommand command) {
        var projectId = loggedUserAccessor.getProjectId();
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        listingService.verifyAccess(project, Stream.of(Path.of(command.getFolder(), "any").toString()));
        var subfolders = listingRepository.findSubfolders(project, command.getFolder());
        return new SubfoldersDto(subfolders.stream().sorted(String::compareTo).toList());
    }
}
