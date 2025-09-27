package net.cycastic.sigil.application.partition.member.add;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.listing.PartitionUser;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AddPartitionMemberCommandHandler implements Command.Handler<AddPartitionMemberCommand, Void> {
    private final PartitionService partitionService;
    private final UserRepository userRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final CipherRepository cipherRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(AddPartitionMemberCommand command) {
        Objects.requireNonNull(command.getWrappedPartitionUserKey());

        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE);
        var user = userRepository.findByEmailAndTenantId(command.getEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        if (loggedUserAccessor.getUserId() == user.getId()){
            throw RequestException.withExceptionCode("C400T010");
        }
        var partition = partitionService.getPartition();
        var cipher = new Cipher(CipherDecryptionMethod.UNWRAPPED_USER_KEY, null, Base64.getDecoder().decode(command.getWrappedPartitionUserKey()));
        cipherRepository.save(cipher);
        var partitionUser = PartitionUser.builder()
                .partition(partition)
                .user(user)
                .partitionUserKey(cipher)
                .permissions(command.getPermissions())
                .build();
        partitionUserRepository.save(partitionUser);

        return null;
    }
}
