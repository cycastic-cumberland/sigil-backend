package net.cycastic.sigil.application.partition.member.remove;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;

@Data
@TransactionalCommand
public class RemovePartitionMemberCommand implements Command<Void> {
    @NotNull
    @Email
    private String email;
}
