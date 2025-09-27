package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;

@Data
public class CompletePasswordBasedCipher {
    @NotNull
    private String keyDerivationSettings;

    @NotNull
    private CipherDto cipher;
}
