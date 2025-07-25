package net.cycastic.sigil.application.partition.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.application.cipher.CipherService;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.PartitionUser;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CreatePartitionCommandHandler implements Command.Handler<CreatePartitionCommand, IdDto> {
    private static final Pattern INVALID_PATH = Pattern.compile("\\\\|/{2}|_|%|/_/");

    private final TenantService tenantService;
    private final CipherService cipherService;
    private final PartitionRepository partitionRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final CipherRepository cipherRepository;
    private final UserService userService;

    @Override
    @Transactional
    @SneakyThrows
    public IdDto handle(CreatePartitionCommand command) {
        var path = command.getPartitionPath();
        if (!path.startsWith("/")){
            throw new RequestException(400, "Partition path must start with a forward slash");
        }
        if (path.endsWith("/")){
            throw new RequestException(400, "Partition path must not end with a forward slash");
        }
        if (INVALID_PATH.matcher(path).find()) {
            throw new RequestException(400, "Path must not contain \\, //, _, % or /_/");
        }

        tenantService.checkPermission(ApplicationConstants.TenantPermissions.CREATE_PARTITIONS);
        var tenant = tenantService.getTenant();

        var partition = Partition.builder()
                .tenant(tenant)
                .partitionPath(command.getPartitionPath())
                .createdAt(OffsetDateTime.now())
                .build();
        var partitionKey = new byte[CryptographicUtilities.KEY_LENGTH];
        CryptographicUtilities.generateRandom(partitionKey);
        var listingKey = partitionKey;

        var user = userService.getUser();
        var userPublicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(user.getPublicRsaKey());
        var userPartitionKey = CryptographicUtilities.encrypt(userPublicKey, partitionKey);
        var partitionUserKey = new Cipher(CipherDecryptionMethod.UNWRAPPED_USER_KEY, userPartitionKey);

        if (command.isServerSideKeyDerivation()){
            var serverPartitionKey = new byte[CryptographicUtilities.KEY_LENGTH];
            CryptographicUtilities.generateRandom(serverPartitionKey);
            listingKey = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, partitionKey, serverPartitionKey);
            var serverPartitionCipher = cipherService.createServerManagedKey(serverPartitionKey);

            cipherRepository.save(serverPartitionCipher);
            partition.setServerPartitionKey(serverPartitionCipher);
        }
        partition.setKeyMd5Digest(CryptographicUtilities.digestMd5(listingKey));
        partition.setKeySha256Digest(CryptographicUtilities.digestSha256(listingKey));

        cipherRepository.save(partitionUserKey);
        partitionRepository.save(partition);
        var partitionUser = PartitionUser.builder()
                .user(user)
                .partition(partition)
                .partitionUserKey(partitionUserKey)
                .permissions(ApplicationConstants.PartitionPermissions.READ | ApplicationConstants.PartitionPermissions.WRITE | ApplicationConstants.PartitionPermissions.MODERATE)
                .build();
        partitionUserRepository.save(partitionUser);
        return new IdDto(partition.getId());
    }
}
