package net.cycastic.sigil.domain.dto.keyring;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import jakarta.annotation.Nullable;

import java.util.Base64;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CipherDto {
    @NotNull
    private CipherDecryptionMethod decryptionMethod;

    @Nullable
    private String iv;

    @NotNull
    private String cipher;

    protected void buildFromDomain(Cipher cipher){
        var encoder = Base64.getEncoder();
        decryptionMethod = cipher.getDecryptionMethod();
        iv = cipher.getIv() == null ? null : encoder.encodeToString(cipher.getIv());
        this.cipher = encoder.encodeToString(cipher.getCipher());
    }

    protected static <T extends CipherDto> T fromDomainInternal(T cipherDto, Cipher cipher){
        cipherDto.buildFromDomain(cipher);
        return cipherDto;
    }

    public static CipherDto fromDomain(Cipher cipher){
        return fromDomainInternal(new CipherDto(), cipher);
    }
}
