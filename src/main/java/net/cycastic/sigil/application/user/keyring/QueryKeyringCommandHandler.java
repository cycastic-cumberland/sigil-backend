package net.cycastic.sigil.application.user.keyring;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.keyring.*;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class QueryKeyringCommandHandler implements Command.Handler<QueryKeyringCommand, KeyringDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final PartitionService partitionService;
    private final TenantService tenantService;
    private final CipherRepository cipherRepository;

    public Stream<KeyMaterialDto> getQuerySpecificKeyMaterials(QueryKeyringCommand command){
        Stream<KeyMaterialDto> stream = Stream.empty();
        if (loggedUserAccessor.tryGetPartitionId().isEmpty()){
            var allTenantPartitionKeys = tenantService.tryGetTenant()
                    .stream()
                    .flatMap(t -> cipherRepository.getKeyringByUserIdAndTenant(loggedUserAccessor.getUserId(), t).stream())
                    .map(PartitionKeyMaterialDto::fromDomain)
                    .toList() // materialize
                    .stream();
            stream = Stream.concat(stream, allTenantPartitionKeys);
        } else {
            var partitionKey = partitionService.tryGetPartition()
                    .stream()
                    .flatMap(p -> cipherRepository.getKeyByUserIdAndPartition(loggedUserAccessor.getUserId(), p).stream())
                    .map(PartitionKeyMaterialDto::fromDomain)
                    .toList() // materialize
                    .stream();
            stream = Stream.concat(stream, partitionKey);
        }
        return stream;
    }

    @Override
    public KeyringDto handle(QueryKeyringCommand command) {
        var passwordStream = userRepository.getPasswordBasedKdfDetails(loggedUserAccessor.getUserId())
                .stream()
                .map(PbkdfDerivedKeyMaterialDto::fromDomain);
        var webAuthnStream = userRepository.getWebAuthnBasedKdfDetails(loggedUserAccessor.getUserId())
                .stream()
                .map(PrfDerivedKeyMaterialDto::fromDomain);
        var querySpecific = getQuerySpecificKeyMaterials(command);
        var stream = Stream.concat(passwordStream, webAuthnStream);
        stream = Stream.concat(stream, querySpecific);
        stream = stream.distinct()
                .sorted(Comparator.comparingLong(KeyMaterialDto::getId));
        return KeyringDto.builder()
                .keys(stream.toList())
                .build();
    }
}
