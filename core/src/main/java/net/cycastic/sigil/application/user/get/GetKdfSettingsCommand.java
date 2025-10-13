package net.cycastic.sigil.application.user.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.auth.KdfDetailsDto;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;

import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetKdfSettingsCommand implements Command<KdfDetailsDto> {
    @NotBlank
    private String userEmail;
    private AuthenticationMethod method;

    public String getUserEmail(){
        return userEmail.toUpperCase(Locale.ROOT);
    }
}
