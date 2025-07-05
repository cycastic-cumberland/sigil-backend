package net.cycastic.sigil.application.email.smtp.query;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.BaseSmtpCredentialDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuerySmtpCredentialsCommand extends PageRequestDto implements Command<PageResponseDto<BaseSmtpCredentialDto>> {
}
