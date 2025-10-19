package net.cycastic.sigil.application.partition.member.remove;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemovePartitionMemberCommandHandler implements Command.Handler<RemovePartitionMemberCommand, Void> {
    private final PartitionService partitionService;
    private final UserRepository userRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(RemovePartitionMemberCommand command) {
        var user = userRepository.findByEmailAndTenantId(command.getEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        if (!user.getId().equals(loggedUserAccessor.getUserId())){
            partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE);
        }

        partitionUserRepository.removeByPartitionAndUser(partitionService.getPartition(), user);
        return null;
    }
}
