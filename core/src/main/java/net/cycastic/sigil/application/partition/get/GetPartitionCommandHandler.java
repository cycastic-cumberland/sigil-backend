package net.cycastic.sigil.application.partition.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.dto.listing.PartitionDto;
import net.cycastic.sigil.domain.dto.listing.ProjectPartitionDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPartitionCommandHandler implements Command.Handler<GetPartitionCommand, PartitionDto> {
    private final PartitionRepository partitionRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PartitionUserRepository partitionUserRepository;

    @Override
    public PartitionDto handle(GetPartitionCommand command) {
        var partition = partitionRepository.findByTenant_IdAndPartitionPath(loggedUserAccessor.getTenantId(), command.getPartitionPath())
                .orElseThrow(() -> new RequestException(404, "Partition not found"));
        var partitionUser = partitionUserRepository.findByPartition_IdAndUser_Id(partition.getId(),
                        loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        PartitionDto partitionDto;
        if (partition.getPartitionType().equals(PartitionType.PROJECT)){
            partitionDto = ProjectPartitionDto.fromDomain(partition);
        } else {
            partitionDto = PartitionDto.fromDomain(partition);
        }

        var cipher = CipherDto.fromDomain(partitionUser.getPartitionUserKey());
        partitionDto.setUserPartitionKey(cipher);
        partitionDto.setPermissions(ApplicationConstants.PartitionPermissions.toReadablePermissions(partitionUser.getPermissions()));
        return partitionDto;
    }
}
