package net.cycastic.sigil.application.partition.member.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPartitionMemberCommandHandler implements Command.Handler<GetPartitionMemberCommand, PartitionUserDto> {
    private final UserRepository userRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PartitionUserDto handle(GetPartitionMemberCommand command) {
        var user = userRepository.findByEmailAndTenantId(command.getEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var partitionUser = partitionUserRepository.getByPartitionAndUser(loggedUserAccessor.getPartitionId(), user.getId())
                .orElseThrow(() -> new RequestException(404, "User is not a member of partition"));
        return PartitionUserDto.fromDomain(partitionUser);
    }
}
