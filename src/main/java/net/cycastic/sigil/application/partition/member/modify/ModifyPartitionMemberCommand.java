package net.cycastic.sigil.application.partition.member.modify;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class ModifyPartitionMemberCommand implements Command<Void> {
    private String email;
    private int permissions;
}
