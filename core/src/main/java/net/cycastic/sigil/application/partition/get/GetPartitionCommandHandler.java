package net.cycastic.sigil.application.partition.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.PartitionsController;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.dto.listing.PartitionDto;
import net.cycastic.sigil.domain.dto.listing.ProjectPartitionDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetPartitionCommandHandler implements Command.Handler<GetPartitionCommand, PartitionDto> {
    private final GetPartitionCommandInternalHandler getPartitionCommandInternalHandler;

    @Component
    @RequiredArgsConstructor
    public static class GetPartitionCommandInternalHandler {
        private final PartitionUserRepository partitionUserRepository;
        private final PartitionRepository partitionRepository;

        @Caching(
                cacheable = {
                        @Cacheable(value = PartitionsController.CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                                key = "'getPartition' + '?tenantId=' + #tenantId + '&userId=' + #userId + '&partitionPath=' + #command.partitionPath",
                                condition = "#tenantId != null && #command.id == null && #command.partitionPath != null"),
                        @Cacheable(value = PartitionsController.CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                                key = "'getPartition' + '?tenantId=' + #tenantId + '&userId=' + #userId + '&id=' + #command.id",
                                condition = "#tenantId != null && #command.id != null && #command.partitionPath == null")
                }
        )
        public PartitionDto handle(GetPartitionCommand command, int userId, int tenantId){
            Optional<Partition> partitionOpt;
            if (command.getId() != null){
                partitionOpt = partitionRepository.findByTenant_IdAndId(tenantId, command.getId());
            } else {
                partitionOpt = partitionRepository.findByTenant_IdAndPartitionPath(tenantId, command.getPartitionPath());
            }

            var partition = partitionOpt.orElseThrow(() -> new RequestException(404, "Partition not found"));
            var partitionUser = partitionUserRepository.findByPartition_IdAndUser_Id(partition.getId(),
                            userId)
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

    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PartitionDto handle(GetPartitionCommand command) {
        return getPartitionCommandInternalHandler.handle(command, loggedUserAccessor.getUserId(), loggedUserAccessor.getTenantId());
    }
}
