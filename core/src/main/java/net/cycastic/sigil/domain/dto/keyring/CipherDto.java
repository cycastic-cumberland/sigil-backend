package net.cycastic.sigil.domain.dto.keyring;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import jakarta.annotation.Nullable;

import java.util.Base64;
import java.util.Objects;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CipherDto {
    @NotNull
    private CipherDecryptionMethod decryptionMethod;

    @Nullable
    @Base64String
    private String iv;

    @NotNull
    @Base64String
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

    public Cipher toDomain(boolean requireIv){
        return new Cipher(decryptionMethod,
                requireIv
                    ? Base64.getDecoder().decode(Objects.requireNonNull(iv, () -> {throw new RequestException(400, "IV not supplied");}))
                    : iv == null
                        ? null
                        : Base64.getDecoder().decode(iv),
                Base64.getDecoder().decode(cipher));
    }

    public Cipher toDomain(){
        return toDomain(false);
    }
}
