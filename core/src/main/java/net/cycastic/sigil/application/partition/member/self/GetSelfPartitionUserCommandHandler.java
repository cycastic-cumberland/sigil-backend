package net.cycastic.sigil.application.partition.member.self;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSelfPartitionUserCommandHandler implements Command.Handler<GetSelfPartitionUserCommand, PartitionUserDto> {
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PartitionUserDto handle(GetSelfPartitionUserCommand command) {
        var p = partitionUserRepository.getByPartitionAndUser(loggedUserAccessor.getPartitionId(), loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        return PartitionUserDto.fromDomain(p);
    }
}
