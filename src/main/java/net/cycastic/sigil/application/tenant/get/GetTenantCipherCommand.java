package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.CipherDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTenantCipherCommand implements Command<CipherDto> {
    private int id;
}
