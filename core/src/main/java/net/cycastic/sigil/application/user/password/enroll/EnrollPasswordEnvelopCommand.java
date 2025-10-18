package net.cycastic.sigil.application.user.password.enroll;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.user.validation.rewrap.ValidateUserKeyRewrap;
import net.cycastic.sigil.domain.dto.auth.CompletePasswordBasedCipher;

@Data
@TransactionalCommand
@EqualsAndHashCode(callSuper = true)
public class EnrollPasswordEnvelopCommand extends CompletePasswordBasedCipher implements Command<Void>, ValidateUserKeyRewrap {
    private String signatureAlgorithm;

    private String signature;

    @JsonIgnore
    public String getCiphertext(){
        return getCipher().getCipher();
    }
}
