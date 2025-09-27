package net.cycastic.sigil.application.user.keyring;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.keyring.KeyringDto;

@Data
public class QueryKeyringCommand implements Command<KeyringDto> {
}
