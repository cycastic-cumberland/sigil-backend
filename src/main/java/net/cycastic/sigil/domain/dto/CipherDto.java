package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;

import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CipherDto {
    private CipherDecryptionMethod decryptionMethod;
    private String kid;
    private String iv;
    private String cipher;

    public static CipherDto fromDomain(Cipher cipher){
        var encoder = Base64.getEncoder();
        return CipherDto.builder()
                .decryptionMethod(cipher.getDecryptionMethod())
                .kid(encoder.encodeToString(cipher.getKid()))
                .iv(cipher.getIv() == null ? null : encoder.encodeToString(cipher.getIv()))
                .cipher(encoder.encodeToString(cipher.getCipher()))
                .build();
    }
}
