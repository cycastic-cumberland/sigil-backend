package net.cycastic.sigil.application.partition;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartitionService {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final PartitionRepository partitionRepository;
    private final PartitionUserRepository partitionUserRepository;

    public void checkPermission(int partitionId, int mask){
        if (loggedUserAccessor.isAdmin()){
            return;
        }
        var partitionUser = partitionUserRepository.findByPartition_IdAndUser_Id(partitionId,
                        loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        var tenant = tenantRepository.findByPartitionUser(partitionUser);
        if (tenant.getOwner().getId().equals(partitionUser.getUser().getId())){
            return;
        }

        if ((partitionUser.getPermissions() & mask) != mask){
            throw RequestException.forbidden();
        }
    }

    public void checkPermission(int mask){
        checkPermission(loggedUserAccessor.getPartitionId(), mask);
    }

    public Optional<Partition> tryGetPartition(){
        var idOpt = loggedUserAccessor.tryGetPartitionId();
        if (idOpt.isEmpty()){
            return Optional.empty();
        }

        try {
            var partition = partitionRepository.getReferenceById(idOpt.getAsInt());
            return Optional.of(partition);
        } catch (EntityNotFoundException ignored){
            return idOpt.stream()
                    .boxed()
                    .flatMap(id -> partitionRepository.findById(id).stream())
                    .findFirst();
        }
    }

    public Partition getPartition(){
        return tryGetPartition()
                .orElseThrow(() -> new RequestException(404, "Partition not found"));
    }
}
