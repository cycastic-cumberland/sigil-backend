package net.cycastic.sigil.application.user.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.auth.EnvelopDto;

import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetEnvelopCommand implements Command<EnvelopDto> {
    @NotEmpty
    private String userEmail;

    public String getUserEmail(){
        return userEmail.toUpperCase(Locale.ROOT);
    }
}
