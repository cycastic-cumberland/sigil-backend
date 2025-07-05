package net.cycastic.sigil.configuration;

import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class BaseJwtConfiguration {
    private long validForMillis;
    @Null
    private String issuer;
}
