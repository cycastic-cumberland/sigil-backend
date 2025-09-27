package net.cycastic.sigil.domain.dto.keyring;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class KeyringDto {
    private Collection<KeyMaterialDto> keys;
}
