package net.cycastic.sigil.application.partition.member.remove;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class RemovePartitionMemberCommand implements Command<@Null Object> {
    private String email;
}
