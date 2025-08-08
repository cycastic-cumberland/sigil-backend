package net.cycastic.sigil.domain.dto.keyring;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.Cipher;

@Data
@SuperBuilder
@NoArgsConstructor
public class KeyMaterialDto extends CipherDto {
    private long id;

    @Override
    protected void buildFromDomain(Cipher cipher) {
        super.buildFromDomain(cipher);
        id = cipher.getId();
    }

    public static KeyMaterialDto fromDomain(Cipher cipher){
        return fromDomainInternal(new KeyMaterialDto(), cipher);
    }

    @Override
    public int hashCode(){
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof KeyMaterialDto km &&
                km.getId() == getId();
    }
}
