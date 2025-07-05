package net.cycastic.sigil.application.email.smtp.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class SaveSmtpCredentialCommand implements Command<IdDto> {
    @Nullable
    private Integer id;

    @NotNull
    private String serverAddress;

    @NotNull
    private String secureSmtp;

    @NotNull
    private int port;

    @NotNull
    private int timeout;

    @NotNull
    private String fromAddress;

    @NotNull
    private String password;

    @NotNull
    private String fromName;
}
