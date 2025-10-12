package net.cycastic.sigil.service.encryption;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AEADEncryptedChunk {
    private String path;
    private String iv;
}
