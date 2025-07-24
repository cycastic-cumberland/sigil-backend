package net.cycastic.sigil.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import org.springframework.lang.Nullable;

import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CipherDto {
    @NotNull
    private CipherDecryptionMethod decryptionMethod;

    @Nullable
    private String iv;

    @NotNull
    private String cipher;

    public static CipherDto fromDomain(Cipher cipher){
        var encoder = Base64.getEncoder();
        return CipherDto.builder()
                .decryptionMethod(cipher.getDecryptionMethod())
                .iv(cipher.getIv() == null ? null : encoder.encodeToString(cipher.getIv()))
                .cipher(encoder.encodeToString(cipher.getCipher()))
                .build();
    }
}
