package net.cycastic.portfoliotoolkit.application.email.smtp.query;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.dto.BaseSmtpCredentialDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageRequestDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuerySmtpCredentialsCommand extends PageRequestDto implements Command<PageResponseDto<BaseSmtpCredentialDto>> {
}
