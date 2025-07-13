package net.cycastic.sigil.application.partition.member.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryPartitionMemberCommandHandler implements Command.Handler<QueryPartitionMemberCommand, PageResponseDto<PartitionUserDto>> {
    private final PartitionUserRepository partitionUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PageResponseDto<PartitionUserDto> handle(QueryPartitionMemberCommand command) {
        var page = partitionUserRepository.queryByPartition(loggedUserAccessor.getPartitionId(), command.toPageable());
        return PageResponseDto.fromDomain(page,
                p -> PartitionUserDto.builder()
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .email(p.getEmail())
                        .permissions(ApplicationConstants.PartitionPermissions.toReadablePermissions(p.getPermissions()))
                        .build());
    }
}
