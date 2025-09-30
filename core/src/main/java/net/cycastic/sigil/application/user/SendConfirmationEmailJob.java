package net.cycastic.sigil.application.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.UserDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendConfirmationEmailJob {
    private UserDto user;
    private String securityStamp;
}
