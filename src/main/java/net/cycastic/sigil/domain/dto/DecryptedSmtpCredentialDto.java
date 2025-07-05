package net.cycastic.sigil.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.cycastic.sigil.domain.model.EncryptedSmtpCredential;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DecryptedSmtpCredentialDto extends BaseSmtpCredentialDto{
    @NotNull
    @ToString.Exclude
    private String fromAddress;

    @NotNull
    @ToString.Exclude
    private String password;

    public DecryptedSmtpCredentialDto(EncryptedSmtpCredential credential,
                                      String fromAddress,
                                      String password){
        super(credential);
        this.fromAddress = fromAddress;
        this.password = password;
    }
}
