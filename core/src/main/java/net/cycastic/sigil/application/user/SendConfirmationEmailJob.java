package net.cycastic.sigil.application.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.service.job.BackgroundJob;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendConfirmationEmailJob implements BackgroundJob {
    private UserDto user;
    private String securityStamp;
}
