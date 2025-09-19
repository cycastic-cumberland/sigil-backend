package net.cycastic.sigil.application.partition.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PartitionChecksumValidator implements CommandValidator {
    private final PartitionService partitionService;

    @Override
    public void validate(Command command) {
        var checksumCommand = (PartitionChecksum)command;

        var checksumBase64 = checksumCommand.getPartitionChecksum();
        var checksum = Base64.getDecoder().decode(checksumBase64);

        var partition = partitionService.getPartition();
        if (!CryptographicUtilities.constantTimeEquals(checksum, checksumCommand.isMd5() ? partition.getKeyMd5Digest() : partition.getKeySha256Digest())){
            throw RequestException.withExceptionCode("C400T006");
        }
    }

    @Override
    public boolean matches(Class klass) {
        return PartitionChecksum.class.isAssignableFrom(klass);
    }
}
