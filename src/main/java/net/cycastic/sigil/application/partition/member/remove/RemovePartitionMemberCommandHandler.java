package net.cycastic.sigil.application.partition.member.remove;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemovePartitionMemberCommandHandler implements Command.Handler<RemovePartitionMemberCommand, @Null Object> {
    private final PartitionService partitionService;
    private final UserRepository userRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public @Null Object handle(RemovePartitionMemberCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE);
        var user = userRepository.findByEmailAndTenantId(command.getEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        if (loggedUserAccessor.getUserId() == user.getId()){
            throw new RequestException(400, "Invalid request");
        }
        partitionUserRepository.removeByPartitionAndUser(partitionService.getPartition(), user);

        return null;
    }
}
