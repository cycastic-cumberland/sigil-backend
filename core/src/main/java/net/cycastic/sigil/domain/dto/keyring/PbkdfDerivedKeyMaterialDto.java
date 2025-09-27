package net.cycastic.sigil.domain.dto.keyring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.keyring.interfaces.CipherBasedKdfDetails;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;

import java.util.Base64;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PbkdfDerivedKeyMaterialDto extends KeyMaterialDto{
    public static PbkdfDerivedKeyMaterialDto fromDomain(CipherBasedKdfDetails domain){
        return PbkdfDerivedKeyMaterialDto.builder()
                .id(domain.getCipherId())
                .decryptionMethod(CipherDecryptionMethod.USER_PASSWORD)
                .iv(domain.getIv() != null ? Base64.getEncoder().encodeToString(domain.getIv()) : null)
                .cipher(Base64.getEncoder().encodeToString(domain.getCipher()))
                .build();
    }
}
