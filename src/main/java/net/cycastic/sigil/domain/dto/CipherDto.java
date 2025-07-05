package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherEncryptionMethod;

import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CipherDto {
    private long id;
    private CipherEncryptionMethod decryptionMethod;
    private String kid;
    private String iv;
    private String cipher;

    public static CipherDto fromDomain(Cipher cipher){
        var encoder = Base64.getEncoder();
        return CipherDto.builder()
                .id(cipher.getId())
                .decryptionMethod(cipher.getDecryptionMethod())
                .kid(encoder.encodeToString(cipher.getKid()))
                .iv(encoder.encodeToString(cipher.getIv()))
                .cipher(encoder.encodeToString(cipher.getCipher()))
                .build();
    }
}
