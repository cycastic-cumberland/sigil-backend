package net.cycastic.portfoliotoolkit.application.listing.acp.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.SimpleDiffUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.ListingAccessControlPolicy;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingACPRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SaveLACPCommandHandler implements Command.Handler<SaveLACPCommand, @Null Object> {
    private static final SimpleDiffUtilities<ListingAccessControlPolicy, Integer> DIFF = SimpleDiffUtilities.<ListingAccessControlPolicy, Integer>builder()
            .keySelector(ListingAccessControlPolicy::getPriority)
            .comparator((lhs, rhs) -> lhs.isAllowed() == rhs.isAllowed() &&
                    lhs.getApplyToId().equals(rhs.getApplyToId()) &&
                    lhs.getGlobPath().equals(rhs.getGlobPath()))
            .build();

    private final ProjectRepository projectRepository;
    private final ListingACPRepository listingACPRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    @Transactional
    public @Null Object handle(SaveLACPCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var user = project.getUser();
        if (!loggedUserAccessor.isAdmin() && user.getId() != loggedUserAccessor.getUserId()){
            throw new ForbiddenException();
        }
        if (user.getLacpLimit() != null && command.getPolicies().size() > user.getLacpLimit()){
            throw new RequestException(400, "Can not save any more than %d policies", user.getLacpLimit());
        }
        var accumulator = new AtomicInteger(0);
        var newPolicies = command.getPolicies().stream()
                .map(p -> ListingAccessControlPolicy.builder()
                        .globPath(p.getGlobPath())
                        .priority(accumulator.getAndIncrement())
                        .isAllowed(p.isAllowed())
                        .project(project)
                        .applyToId(p.getApplyToId())
                        .build())
                .toList();
        var currentPolicies = listingACPRepository.findListingAccessControlPoliciesByProject(project);
        var diff = DIFF.shallowDiff(currentPolicies, newPolicies);
        listingACPRepository.saveAll(diff.getNewEntities());
        for (var updated : diff.getUpdatedEntities()){
            var originalACP = updated.original();
            var updatedACP = updated.updated();

            originalACP.setGlobPath(updatedACP.getGlobPath());
            originalACP.setAllowed(updatedACP.isAllowed());
            originalACP.setApplyToId(updatedACP.getApplyToId());
        }

        listingACPRepository.saveAll(diff.getUpdatedEntities().stream()
                .map(SimpleDiffUtilities.UpdatedEntity::original)
                .toList());
        listingACPRepository.deleteByIdIsIn(diff.getDeletedEntities().stream()
                .map(ListingAccessControlPolicy::getId)
                .toList());
        return null;
    }
}
