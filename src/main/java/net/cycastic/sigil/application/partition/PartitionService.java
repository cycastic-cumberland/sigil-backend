package net.cycastic.sigil.application.partition;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.Partition;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartitionService {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final PartitionRepository partitionRepository;
    private final PartitionUserRepository partitionUserRepository;

    public void checkPermission(int mask){
        var partitionUser = partitionUserRepository.findByPartition_IdAndUser_Id(loggedUserAccessor.getPartitionId(),
                loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        var tenant = tenantRepository.findByPartitionUser(partitionUser)
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));
        if (tenant.getOwner().getId().equals(partitionUser.getUser().getId())){
            return;
        }

        if ((partitionUser.getPermissions() & mask) != mask){
            throw RequestException.forbidden();
        }
    }

    public Partition getPartition(){
        return partitionRepository.findById(loggedUserAccessor.getPartitionId())
                .orElseThrow(() -> new RequestException(404, "Partition not found"));
    }
}
