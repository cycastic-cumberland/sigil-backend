package net.cycastic.sigil.domain.dto.keyring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.keyring.interfaces.PartitionKeyMaterial;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;

import java.util.Base64;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartitionKeyMaterialDto extends KeyMaterialDto {
    private int partitionId;

    public static PartitionKeyMaterialDto fromDomain(PartitionKeyMaterial domain){
        return PartitionKeyMaterialDto.builder()
                .id(domain.getCipherId())
                .partitionId(domain.getPartitionId())
                .decryptionMethod(CipherDecryptionMethod.UNWRAPPED_USER_KEY)
                .iv(domain.getIv() != null ? Base64.getEncoder().encodeToString(domain.getIv()) : null)
                .cipher(Base64.getEncoder().encodeToString(domain.getCipher()))
                .build();
    }
}
