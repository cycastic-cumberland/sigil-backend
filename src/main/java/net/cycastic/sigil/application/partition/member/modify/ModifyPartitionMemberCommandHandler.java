package net.cycastic.sigil.application.partition.member.modify;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
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
public class ModifyPartitionMemberCommandHandler implements Command.Handler<ModifyPartitionMemberCommand, Void> {
    private final PartitionService partitionService;
    private final UserRepository userRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(ModifyPartitionMemberCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE);
        var user = userRepository.findByEmailAndTenantId(command.getEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var partitionUser = partitionUserRepository.findByPartition_IdAndUser_Id(loggedUserAccessor.getPartitionId(), user.getId())
                .orElseThrow(() -> new RequestException(404, "User is not a member of partition"));
        partitionUser.setPermissions(command.getPermissions());
        partitionUserRepository.save(partitionUser);
        return null;
    }
}
