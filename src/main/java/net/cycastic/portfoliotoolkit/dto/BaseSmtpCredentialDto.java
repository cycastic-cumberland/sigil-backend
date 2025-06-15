package net.cycastic.portfoliotoolkit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.EncryptedSmtpCredential;

@Data
@NoArgsConstructor
public class BaseSmtpCredentialDto {
    private int id;

    @NotNull
    private String serverAddress;

    @NotNull
    private String secureSmtp;

    @NotNull
    private int port;

    @NotNull
    private int timeout;

    @NotNull
    private String fromName;

    public BaseSmtpCredentialDto(EncryptedSmtpCredential credential){
        id = credential.getId();
        serverAddress = credential.getServerAddress();
        secureSmtp = credential.getSecureSmtp();
        port = credential.getPort();
        timeout = credential.getTimeout();
        fromName = credential.getFromName();
    }
}
