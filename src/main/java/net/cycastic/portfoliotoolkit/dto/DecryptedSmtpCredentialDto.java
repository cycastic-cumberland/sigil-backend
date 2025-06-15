package net.cycastic.portfoliotoolkit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.EncryptedSmtpCredential;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DecryptedSmtpCredentialDto extends BaseSmtpCredentialDto{
    @NotNull
    private String fromAddress;

    @NotNull
    private String password;

    public DecryptedSmtpCredentialDto(EncryptedSmtpCredential credential,
                                      String fromAddress,
                                      String password){
        super(credential);
        this.fromAddress = fromAddress;
        this.password = password;
    }
}
